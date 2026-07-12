"""
Parses Manila City Council Permanent Employees PDF and generates SQL INSERT script.
Output: insert_employees.sql (118 non-vacant permanent employees, skips VACANT and contractual)
"""

import re
import uuid
import bcrypt
import pdfplumber

PDF_PATH = "MANILA CITY COUNCIL PERMANENT EMPLOYEES.xlsx - as of May 2026.pdf"
SQL_OUTPUT = "insert_employees.sql"
DEFAULT_PASSWORD = b"ChangeMe123"

PASSWORD_HASH = bcrypt.hashpw(DEFAULT_PASSWORD, bcrypt.gensalt(rounds=11)).decode()

# Words that appear in position titles — strip these (left-to-right) to isolate the last name
POSITION_TITLE_WORDS = {
    'CITY', 'GOVERNMENT', 'DEPARTMENT', 'HEAD', 'ASSISTANT', 'SENIOR',
    'ADMINISTRATIVE', 'AIDE', 'OFFICER', 'SUPERVISING', 'CHIEF',
    'LOCAL', 'LEGISLATIVE', 'STAFF',
    # Roman numeral grades at the end of a title
    'I', 'II', 'III', 'IV', 'V', 'VI',
}

# Generational suffixes that may appear between first name and middle initial
GENERATIONAL_SUFFIXES = {'JR.', 'SR.', 'II', 'III', 'IV', 'V', 'VI'}


def is_middle_initial(token: str) -> bool:
    """
    True for tokens like: A.  DC.  D.C.  D.G.  D.J.
    False for JR. SR. (they look like 2-letter initials but are generational suffixes).
    """
    if token in {'JR.', 'SR.'}:
        return False
    # Matches 1-2 uppercase letters (optionally separated by a dot) followed by a period
    return bool(re.match(r'^[A-ZÑ]\.?[A-ZÑ]?\.$', token))


def extract_last_name(text_before_comma: str) -> str:
    """
    Given text like 'Senior Administrative Assistant IV SABAO' or just 'SABAO',
    strip position title words from the left to get the actual last name.
    Handles multi-word last names: DELA CRUZ, SAN JOSE, LAS PIÑAS.
    """
    words = text_before_comma.strip().split()
    while words and words[0].upper() in POSITION_TITLE_WORDS:
        words.pop(0)
    return ' '.join(words)


def parse_name(raw_text: str):
    """
    Parse '[POSITION TITLE] LASTNAME, FIRSTNAME [SUFFIX] MI.' into
    (last_name, first_name, middle_initial).
    """
    raw_text = raw_text.strip()
    if ',' not in raw_text:
        return raw_text, '', ''

    comma_idx = raw_text.index(',')
    text_before = raw_text[:comma_idx]
    rest = raw_text[comma_idx + 1:].strip()

    last_name = extract_last_name(text_before)

    tokens = rest.split()
    if not tokens:
        return last_name, '', ''

    middle_initial = ''

    # Last token: if it looks like a middle initial, extract it
    if tokens and is_middle_initial(tokens[-1]):
        raw_mi = tokens.pop()
        middle_initial = raw_mi.replace('.', '')  # 'D.G.' → 'DG', 'S.' → 'S'

    # New last token: if it's a generational suffix (JR., IV, etc.), remove it
    if tokens and tokens[-1].upper() in GENERATIONAL_SUFFIXES:
        tokens.pop()

    first_name = ' '.join(tokens)
    return last_name, first_name, middle_initial


def extract_employees(pdf_path: str):
    """Return list of (item_no, last_name, first_name, middle_initial) for non-vacant employees."""
    employees = []
    in_contractual = False

    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            text = page.extract_text() or ''
            for line in text.split('\n'):
                line = line.strip()

                if re.match(r'^CONTRACTUAL\b', line, re.IGNORECASE):
                    in_contractual = True
                    continue
                if in_contractual:
                    continue

                m = re.match(r'^(\d+)\s+(.+)$', line)
                if not m:
                    continue

                item_no = m.group(1)
                full_text = m.group(2).strip()

                if 'VACANT' in full_text.upper():
                    continue
                if ',' not in full_text:
                    continue

                last_name, first_name, middle_initial = parse_name(full_text)
                if last_name and first_name:
                    employees.append((item_no, last_name, first_name, middle_initial))

    return employees


def normalize_username(s: str) -> str:
    """Lowercase, transliterate Ñ→n, keep only alphanumeric."""
    s = s.lower().replace('ñ', 'n')
    return re.sub(r'[^a-z0-9]', '', s)


def make_username(first_name: str, last_name: str) -> str:
    """first_initial + last_name, lowercased, ASCII-only (matches EmployeeController logic)."""
    first_initial = normalize_username(first_name[0]) if first_name else ''
    last = normalize_username(last_name)
    return first_initial + last


def generate_sql(employees):
    seen_usernames: dict[str, int] = {}
    lines = [
        '-- Manila City Council Permanent Employees — bulk import',
        '-- Source: MANILA CITY COUNCIL PERMANENT EMPLOYEES.xlsx - as of May 2026.pdf',
        '-- Default login password: ChangeMe123',
        '-- Username format: first_initial + last_name (lowercase)',
        '-- birthdate: NULL (PDF has no birth dates)',
        f'-- Total rows: {len(employees)}',
        '',
        'SET NAMES utf8mb4;',
        '',
    ]

    for item_no, last_name, first_name, middle_initial in employees:
        base = make_username(first_name, last_name)
        if base in seen_usernames:
            seen_usernames[base] += 1
            username = f'{base}{seen_usernames[base]}'
        else:
            seen_usernames[base] = 1
            username = base

        emp_hash = str(uuid.uuid4()).replace('-', '')[:30]

        def q(s):
            return s.replace("'", "''") if s else ''

        mi_sql = f"'{q(middle_initial)}'" if middle_initial else 'NULL'

        lines.append(
            f"INSERT IGNORE INTO employee "
            f"(first_name, last_name, middle_name, birthdate, plantilla_no, emp_no, "
            f"username, password, emp_hash_code, user_type, status) VALUES ("
            f"'{q(first_name)}', '{q(last_name)}', {mi_sql}, NULL, "
            f"'{item_no}', '{item_no}', "
            f"'{username}', '{PASSWORD_HASH}', '{emp_hash}', "
            f"'ROLE_EMPLOYEE', 'ACTIVE');"
        )

    return '\n'.join(lines)


def main():
    print(f'Parsing: {PDF_PATH}')
    employees = extract_employees(PDF_PATH)
    print(f'Found {len(employees)} non-vacant permanent employees\n')

    print(f'{"#":<4} {"ITEM":<6} {"LAST NAME":<25} {"FIRST NAME":<25} {"MI":<5}')
    print('-' * 70)
    for i, (item_no, last, first, mi) in enumerate(employees, 1):
        print(f'{i:<4} {item_no:<6} {last:<25} {first:<25} {mi:<5}')

    sql = generate_sql(employees)
    with open(SQL_OUTPUT, 'w', encoding='utf-8') as f:
        f.write(sql)
    print(f'\nSQL written to: {SQL_OUTPUT}')


if __name__ == '__main__':
    main()
