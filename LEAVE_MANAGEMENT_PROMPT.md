# Leave Management Analysis Prompt

Use this prompt with Claude to analyze the leave-management documents in the project locally first. Do not upload anything to the server yet. Read the files in `Leave_Docs/` only, then produce a structured understanding of the leave workflow before any implementation.

## Goal
Understand the leave-management process described in the attached documents and translate it into a clear module plan for this HRIS project.

## Important Constraints
- Work locally first inside the project directory.
- Do not make code changes in this step.
- Do not discuss attendance or payroll processing unless the document explicitly mentions them as file-routing or reference context.
- Focus only on leave management, leave application export, and leave credit tracking.
- Keep context usage low by reading the documents in chunks and summarizing each chunk before moving on.
- Use the documents in `Leave_Docs/` as the source of truth.

## Chunks to Read

### Chunk 1: Procedure Manual First
Read `Leave_Docs/PROCEDURES-MANUAL-FAD-LEAVE-PROCESSING.pdf` first.

Extract only the workflow steps and roles:
- objective
- scope
- responsible persons
- definitions
- records generated
- process flow
- approval chain
- filing and retention steps

Summarize the process as a simple step-by-step leave workflow.

### Chunk 2: Printable Leave Application Form
Next read `Leave_Docs/CS Form No. 6, Revised 2020 (Application for Leave) (Fillable) (2).pdf`.

Extract only the fields and printable layout requirements:
- employee identity fields
- filing date
- position and salary
- leave type options
- leave details section
- working days and inclusive dates
- commutation choice
- certification, recommendation, approval, and disapproval sections
- signature blocks

Summarize this as the PDF export form requirement.

### Chunk 3: Leave Card Sample
Next read `Leave_Docs/LEAVE_CARD_SAMPLE FOR EMPLOYEES.pdf`.

Extract only what the leave card tracks:
- employee header details
- first day of service
- vacation leave ledger
- sick leave ledger
- earned, absence/undertime, and balance columns
- remarks/history entries
- how balances change over time

Summarize this as the leave credit history and balance ledger requirement.

## After Reading All Chunks
Create a concise synthesis with these sections:
1. What the leave module must do
2. What data must be stored
3. What the printable CS Form 6 PDF must contain
4. What the leave card PDF/view must contain
5. What is out of scope for now

## Output Style
- Be concise and factual.
- Do not invent features not present in the documents.
- If a document is unclear, say so explicitly.
- If a term seems to overlap with attendance or payroll, treat it as reference context only unless the document makes leave processing dependent on it.

## Suggested Final Deliverable
Return a short leave-module blueprint that can later be used to plan:
- database fields
- leave request workflow
- approval workflow
- printable PDF export
- leave card/balance ledger
