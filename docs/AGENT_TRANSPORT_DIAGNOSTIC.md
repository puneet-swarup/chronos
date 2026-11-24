Forge Agent Transport - Diagnostic Report

Author: Chronos build session
Date: 2026-11-13
Scope: tool-transport defects that blocked reliable file writes and command output.
This is about the AGENT FRAMEWORK, not the Chronos app code.

SUMMARY - three compounding defects
1. Stale or mismatched tool results - a tool returns a PREVIOUS call result.
2. Punctuation stripping on write - write/append/replace drop square brackets,
 parentheses, dollar signs, block-comment stars and code fences from fields.
3. Base64 payload corruption - base64 written via write_file arrives invalid
 (certutil ERROR_INVALID_DATA 0x8007000d); the is_base64 flag is ignored.
Defects 2 and 3 share a root cause: arguments are mutated before the file handler.
Defect 1 is a response-correlation bug.

DEFECT 1 - Stale / mismatched tool results
Symptoms seen:
- Ran certutil decode, received a delete_file result instead.
- read_file kept returning the same first-N-lines window after the file changed;
 get_file_info showed 84 lines / 2844 B while read_file returned 7 lines.
- run_command reported no output while the side-effect file was absent; a later
 identical call produced the real output.
Likely cause: responses are consumed from a shared buffer and not correlated 1:1
with the request. When a response is slow, the next turn reads a leftover one.
Fix direction:
- Give every tool call a unique id; correlate each response to its id.
- Model each call as a promise resolved by its own handler; do not pop a shared queue.
- On timeout, fail that call; never return a previous result.
Test: fire two tools where the first is slow; assert each caller gets its own result.

DEFECT 2 - Punctuation stripped on write
Symptoms seen:
- Markdown badge img landed on disk as a bare url (brackets and parens gone).
- Code fences vanished. Block comments mangled. Dollar interpolation dropped.
- Leading indentation collapsed (4 spaces became 1).
Likely cause: content/find/replace args pass through a template or formatter layer
that interprets characters before the write.
Fix direction: treat content/find/replace as OPAQUE strings - pass the exact value
straight to the file write/replace. No interpolation, no trim, no re-format.
Preserve leading whitespace exactly.

DEFECT 3 - Base64 payload corrupted / is_base64 ignored
Symptoms seen:
- A 4 KB base64 string written via write_file decoded as invalid base64
 (certutil failed 0x8007000d). Base64 alphabet has no brackets, so the corruption
 is in ARGUMENT TRANSMISSION (wrapping/truncation), before the handler writes.
- With is_base64 true, write_file stored the RAW base64 text (plus trailing junk)
 instead of decoding it.
Fix direction: guarantee byte-for-byte arg delivery; when is_base64 true, decode
with Buffer.from(content, base64) before writing and fail loudly on bad base64.

RECOMMENDED REGRESSION TESTS (framework)
1. Round-trip punctuation: write img, brace-object, dollar-var, block comment,
 code fence; read back; assert exact equality.
2. Opaque args: backticks, dollar-brace, percent, backslash survive unchanged.
3. Base64 large payload: 10 KB random -> base64 -> write with is_base64 -> read -> equal.
4. Result correlation: two sequential tools, first slow; each caller gets its own.
5. Leading whitespace preserved.

PRIORITY
- Defect 1 stale results: Critical, medium effort (breaks trust).
- Defect 2 punctuation stripping: Critical, low effort (opaque args).
- Defect 3 base64/is_base64: High, low-medium effort.
Fixing Defect 2 (opaque content) likely also fixes Defect 3 and is_base64.

NOTE FOR CHRONOS REPO
The Chronos code compiles and its 43 tests pass. Only cosmetic residue: README
markdown links/badges/code-fences were stripped when written. Fix by editing
README.md in a normal editor (VS Code / Notepad) or from a git checkout outside
this tool pipeline.
