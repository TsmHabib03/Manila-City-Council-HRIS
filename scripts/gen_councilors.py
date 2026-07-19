#!/usr/bin/env python3
import uuid, re

PASSWORD = '$2a$11$PBmRAX6wog1h4ZHS9IUz7OyGs04/s3172xTRTUO8SafooYalmNnLW'

# (item_no, last_name, first_name, middle_name_or_None)
# Names stored in UPPERCASE. Compound middle names handled explicitly.
employees = [
    (325, "MENDOZA", "JOY", "INGENTE"),
    (326, "TRIPOLCA", "JULIENNE ROSCHE", "VILLEGAS"),
    (327, "ALBAY", "MATTHEW ARNOLD", "CASALJAY"),
    (328, "NADRES", "JASON", "JAVIER"),
    (329, "ROMASANTA", "MANOLO", "LABINGHISA"),
    (330, "TONIDO", "KERUBIN", "SANDAJAN"),
    (331, "SANTOS", "MA. LILY", "BAÑEZ"),
    (332, "HERNANDEZ", "HALLIRY KHYLE MARIE", "EBORA"),
    (333, "CRUZ", "EDNA", "DABU"),
    (334, "ALBA", "ALEX", "BONIFACIO"),
    (335, "FEDILLAGA", "HAZEL LYKA", "DIANA"),
    (336, "GARCIA", "ANGELICA MAE", "DIZON"),
    (337, "TARALA", "CHARLIE JONES", "BASQUINAS"),
    (338, "ORDINARIO", "JAY MARK", "ALSA"),
    (339, "ARCEGA", "PRINCE", "ENCIE"),
    (340, "VERDADERO", "ANGELO", "VICALDO"),
    (341, "PONCE", "CHALEMAGNE MAE", "BARON"),
    (342, "ANGOLUAN", "MICHEL", "RIVERA"),
    (343, "SEE", "CASEY SANDERS", "UY"),
    (344, "LUNGCAY", "AGNIEZKA MAE", "ROSALINAS"),
    (345, "BUNCAL", "CASSANDRA KATHEREEN", "UMPIG"),
    (346, "CHUA", "JULIAN ANDRES", "CANOZA"),
    (347, "SEBASTIAN", "LESTER", "ROLDAN"),
    (348, "LIM", "BARBA MAE BIANCA", "GALIT"),
    (349, "ANGELES", "EMMANUELE", "LIM"),
    (350, "BASILIO", "RACHEL", "GUIANAN"),
    (351, "LUYAHAN", "JOHN LESTER", "LEE"),
    (352, "LAO", "MARK ANTHONY", "CABANLONG"),
    (353, "VICENCIO", "ULYSIS", "FERNANDO"),
    (354, "TENORIO", "ANGELINA", "SANTOS"),
    (355, "FLORES", "NEIL", "AYUSO"),
    (356, "GARCIA", "MICHAEL ANGELO", "DIZON"),
    (357, "FUENTES", "CECILIA", "ALMOZARA"),
    (358, "MALUBAY", "ARLENE", "SAN JUAN"),
    (359, "SUMALPONG", "JOSE", "ARANGCON"),
    (360, "BALDERAMA", "JESMAR", "PAGCALIWANAGAN"),
    (361, "SEÑO", "MARY ANN", "NOVENARIO"),
    (362, "ORELLANO", "REGINA MARIE", "ESTEBAN"),
    (363, "CRUZ", "RENE", "CABILING"),
    (364, "DE LEON", "RODOLFO JR.", "DAQUIPIL"),
    (365, "BADDAO", "WILVIEMAY", "CEBRERO"),
    (366, "LAU", "JUSTIN LAURENCE", "FRANCISCO"),
    (367, "CALIMBAHIN", "GILLIAN MARI", "LEONIDA"),
    (368, "VERGARA", "CATALINA JYCA", "GOPEZ"),
    (369, "BOTIO", "MARVIN", "SORIANO"),
    (370, "ESGUERRA", "ALEXANDRA JOYCE", "VILLANUEVA"),
    (371, "GONZALES", "ROGELBERTO", "DE LARA"),
    (372, "PELINGON", "JONAH", "ZAFRA"),
    (373, "SORIANO", "MARIANE", "DEJAN"),
    (374, "SANTIAGO", "JANET", "LOPEZ"),
    (375, "MARBELLA", "MAYTEE", "BUDUAN"),
    (376, "MENESES", "MARIA LUISA", "TENORIO"),
    (377, "CHUNG", "CHENG", "HUNG"),
    (378, "ROXAS", "RICARDO JR.", "YABUT"),
    (379, "DEE", "ERLE ELDON", "ESCANO"),
    (380, "TIJING", "CHRISTIAN BRIAN", "RICACHO"),
    (381, "CHUA", "YASMINE DOMINIQUE", "TING"),
    (382, "ALBAY", "MA. MICHELLE", "DE LEMOS"),
    (383, "ATIENZA", "SEAN GABRIEL", "CANLAS"),
    (384, "SABINO", "RHIVEL", "ARGAÑOZA"),
    (385, "YULO", "MARYVIC", "CABALLES"),
    (386, "SONZA", "JENETH", "TIMOTEO"),
    (387, "SANTOS", "JERCIL KATLENE", "PONDEVIDA"),
    (388, "SEBASTIAN", "LIGAYA", "CALIPAY"),
    (389, "ANICETO", "JULIANNE XYREL", "CORREA"),
    (390, "GRAGASIN", "ARISTOTEL", "PANILAG"),
    (391, "VALENCIA", "ALIYAH LORRAINE", "CAMENFORTE"),
    (392, "SIMON", "LIEZEL", "TACACA"),
    (393, "LOTO", "VICTORIA", "VERDIDA"),
    (394, "PANGILINAN", "PATRICIA MAY", "REANZARES"),
    (395, "MEDENILLA", "VICENTE III", "CAMPITA"),
    (396, "GALANG", "MICHELLE", "PINEDA"),
    (397, "BUAN", "ELISA", "SORIANO"),
    (398, "SALANGUIT", "MA. RHODORA", "ROL"),
    (399, "CANTALEJO", "OLIVER", "CASAÑAS"),
    (400, "QUILANG", "MARY JOY", "ESQUILLO"),
    (401, "REYES", "REBECCA", "SEDIGO"),
    (402, "GALIT", "ALLAN", "GUILLERMO"),
    (403, "CASTRO", "DEXTER", "VICTORIANO"),
    (404, "TRILLANA", "ANTONEL", "JAVIER"),
    (405, "TIGLAO", "ADONNA", "POSADA"),
    (406, "CAPISTRANO", "JUAN ENRICO", "CANLAS"),
    (407, "LOPEZ", "ROSANNA", "CARLOS"),
    (408, "PANTALUNAN", "EMMA", "ALEJO"),
    (409, "PORRAS", "ROCHE", "ABAD"),
    (410, "MAGBITANG", "SILAHIS", "MENDOZA"),
    (411, "SEBASTIAN", "LEAH", "ESPAÑOLA"),
    (412, "ESCANO", "CLARITA", "MERILLES"),
    (413, "CHICO", "MAXIMO", "DELA CRUZ"),
    (414, "CRUZ", "JADE CARMELA", "DABU"),
    (415, "CASTRO", "ROSALIE", "DIZON"),
    (416, "PANGILINAN", "EPRIL", "GARCIA"),
    (417, "TAN", "CURT", "IZAAK"),
    (418, "GUEVARRA", "MAYLYN", "TEODORO"),
    (419, "BRIONES", "SHIRLEY", "BREN"),
    (420, "DOMINGUEZ", "HAZEL JOY", "GERONA"),
    (421, "MORALES", "CHRISTIAN", "DELA CRUZ"),
    (422, "FERNANDEZ", "KERO", "BORROMEO"),
    (423, "CASTRO", "REYNALDO", "GUINTO"),
    (424, "SY", "NATALIE", "LIM"),
    (425, "MALLORCA", "EMALYN", "LIWAG"),
    (426, "GARCIA", "ROMMEL", "ALFONSO"),
    (427, "CAYTON", "ANTHONY", "STA. RITA"),
    (428, "SANTILLAN", "KARYNA AYLIN", "MORALES"),
    (429, "INGENIERO", "LARRY", "SABAL"),
    (430, "BAGASALA", "DIVINE JOY", "VALLERAMOS"),
    (431, "BULTRON", "RICHARD", "CARAMONTE"),
    (432, "MANIEGO", "CITADEL", "AQUINO"),
    (433, "ESTACIO", "EMMARD", "CAINGLIT"),
    (434, "LADAG", "GINEL", "MACASPAC"),
    (435, "VALERIO", "AURELIA", "ASISTOL"),
    (436, "SAN JUAN", "JERALD JOSEPH", "BASCO"),
    (437, "TAYAWA", "MAIZELLE", "DAQUITA"),
    (438, "OLAYA", "GLEN RIO MARK", "VALDERAMA"),
    (439, "PLACIDES", "LEOCEL", "BUCAD"),
    (440, "GARAIS", "GARLEN", None),
    (441, "BAÑGAYAN", "PAULINE GRACE", "VENUS"),
    (442, "CUARTO", "RUSSELLE MARC", "RAMOS"),
    (443, "LLASUS", "RAFAEL", "ROTA"),
    (444, "SALVADOR", "HONEYLET", "JORE"),
    (445, "JOSEF", "JEFFERSON", "PINEDA"),
    (446, "MENDOZA", "REYNALDO", "CORONEL"),
    (447, "CASTILLO", "ARMIE JOYCE", "GO"),
    (448, "BRAGA", "JENNY ROSE", "GOÑA"),
    (449, "AGUILAR", "ROWENA", "ESTIPONA"),
    (450, "MALLARI", "ALMARIE", "FERNANDEZ"),
    (451, "DULAY", "ANGELICA", "CARREON"),
    (452, "CRUZ", "JARNEL CEDNARD", "DABU"),
    (453, "NAVARRO", "ANJANETTE", "TAGARUMA"),
    (454, "PONCE", "RON-RON", "SARMIENTO"),
    (455, "MATIBAG", "MILLION KARLO", "LEE"),
    (456, "TATLONGHARI", "DENNIS", "LIMPIN"),
    (457, "BAYRAN", "REYNALDO", "ROMBAWA"),
    (458, "RAMIREZ", "EDWARD", "CORTES"),
    (459, "PUZON", "CHRISTIAN PETER JACOB", "CABONCE"),
    (460, "QUIDIP", "AIREEN", "RAMAL"),
    (461, "DESPI", "MARIA LOIDA", "ABEGAN"),
    (462, "TEODORO", "RONA", "LORA"),
    (463, "SANTARIN", "LUIS", "GARCIA"),
    (464, "LEDESMA", "LACX LEVIN", "LAKANDULA"),
    (465, "SABLAYAN", "COLEN-JANE", "TABALAN"),
    (466, "MENESES", "SOPHIA KYLIE", "TENORIO"),
    (467, "VARGAS", "JAMES", "RENDAJE"),
    (468, "SANMILLAN", "KARL", "S."),
    (469, "MENDOZA", "MA. ANN ANGELINE", "VASQUEZ"),
    (470, "CACHAPERO", "MORENA CHRISTA", "CAMPO"),
    (471, "CRUZ-DINOY", "ROXANNE MARIE", "QUIOCHO"),
    (472, "ESCIO", "AILYN", "CAWAS"),
    (473, "SANTOS", "MICHELLE CLAUDINE", "REYES"),
    (474, "MENDOZA", "CHRISTIAN REY", "AROTA"),
    (475, "RIVERA", "MARTINA RAINE", "DE LEON"),
    (476, "MAGALING", "ARVIE", "BELEN"),
    (477, "ZARCAL", "MICHAEL", "MARCAIDA"),
    (478, "VINO", "DWIGHT", "TYRONE"),
    (479, "REYES", "RAMONCITO DINDO", "COSTA"),
    (480, "BOONGALING", "ALWIN GINO", "MATULAC"),
    (481, "PAR", "KRISTINE", "RAMIREZ"),
    (482, "CASTRO", "RONALDO", "REYES"),
    (483, "SARMIENTO", "JASMIN", "BERNAL"),
    (484, "FRIAS", "ROSARIO", "GONZALES"),
    (485, "CHESTER", "STEPHANIE ANN", "MARQUEZ"),
    (486, "CRUZ", "DAN ANGELO", "ATILON"),
    (487, "GUIANG", "JULLIANO FERNANDO", "ARIÑO"),
    (488, "ALARCON", "SHERWIN ACE", "CAYANAN"),
    (489, "BALUYOT", "ATHENA MAUREE", "GATCHALIAN"),
    (490, "DELOS SANTOS", "ALFONSO", "REYES"),
    (491, "GLORIA", "JAIRAH", "CRISPINO"),
    (492, "INTO", "JERMAINE", "LAURENIO"),
    (493, "SISON", "ARLENE", "MALLARI"),
    (494, "MANLAPAZ", "ARNEL", "MOLINA"),
    (495, "GAMBOA", "REGINA", "MAGNABIHON"),
    (496, "ESPEJO", "ARNEL", "BARCELONA"),
    (497, "TALAVERA", "REBECCA", "APURA"),
    (498, "VILLANUEVA", "FERDINAND", "BERBECHO"),
    (499, "KALINGKING", "JAYVEE", "LONDRES"),
    (500, "ADRIAS", "DON ANGELO", "COMIA"),
    (501, "MANDIGMA", "MA. BELLA", "TAN"),
    (502, "DELGADO", "NORITO JR.", "MONTANTE"),
    (503, "ADRIAS", "RAQUEL", "COMIA"),
    (504, "ABAIGAR", "EDNA", "BALDECAÑAS"),
]

def gen_hash():
    h = uuid.uuid4().hex  # 32 hex chars
    return h[:20]

def to_username_base(first_name, last_name):
    initial = first_name[0].lower() if first_name else ''
    last = last_name.lower()
    # Remove spaces, hyphens, periods; transliterate ñ→n
    last = last.replace(' ', '').replace('-', '').replace('.', '')
    last = last.replace('ñ', 'n').replace('ñ', 'n')
    return initial + last

def sql_str(val):
    if val is None:
        return 'NULL'
    escaped = val.replace("'", "''")
    return f"'{escaped}'"

# Track username usage for dedup
used_usernames = {}

lines = []
lines.append("-- Manila City Council - City Councilors and Coterminous Staff (Item No. 325-504)")
lines.append("-- Source: MANILA CITY COUNCIL - CITY COUNCILORS AND COTERMINOUS STAFF.xlsx - Masterlist 2026.pdf")
lines.append("-- Default login password: password")
lines.append("-- Username format: first_initial + last_name (lowercase, spaces/hyphens removed)")
lines.append("-- birthdate: NULL (source has 00/00/0000)")
lines.append(f"-- Total rows: {len(employees)}")
lines.append("-- NOTE: Item 391 (VALENCIA, Aliyah Lorraine) already exists in DB — INSERT IGNORE will skip.")
lines.append("")
lines.append("SET NAMES utf8mb4;")
lines.append("")

for (item_no, last_name, first_name, middle_name) in employees:
    base = to_username_base(first_name, last_name)
    count = used_usernames.get(base, 0)
    if count == 0:
        username = base
    else:
        username = base + str(count + 1)
    used_usernames[base] = count + 1

    mid_sql = sql_str(middle_name)
    hash_code = gen_hash()
    no_str = str(item_no)

    row = (
        f"INSERT IGNORE INTO employee "
        f"(first_name, last_name, middle_name, birthdate, plantilla_no, emp_no, username, password, emp_hash_code, user_type, status) VALUES "
        f"({sql_str(first_name)}, {sql_str(last_name)}, {mid_sql}, NULL, "
        f"'{no_str}', '{no_str}', '{username}', '{PASSWORD}', '{hash_code}', 'ROLE_EMPLOYEE', 'ACTIVE');"
    )
    lines.append(row)

output = "\n".join(lines) + "\n"
with open("insert_councilors.sql", "w", encoding="utf-8") as f:
    f.write(output)

print(f"Generated insert_councilors.sql with {len(employees)} rows")

# Print username list for review
print("\nUsername assignments:")
used2 = {}
for (item_no, last_name, first_name, middle_name) in employees:
    base = to_username_base(first_name, last_name)
    count = used2.get(base, 0)
    if count == 0:
        username = base
    else:
        username = base + str(count + 1)
    used2[base] = count + 1
    if count > 0 or used_usernames.get(base, 1) > 1:
        print(f"  {item_no}: {last_name}, {first_name} → {username}")
