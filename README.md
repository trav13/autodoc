## AutoDoc

This is a tool for generating .docx, .xlsx, and .pptx files text files and word, excel, and powerpoint files with proper formating.  See the index.html for complete instructions.

To use: AutoDoc is runnable by selecting "Open.." or double-clicking autodoc-v1.7.jar.  You may also download the file directly from GitHub by clicking on "View RAW".

### NEW: Watch Mode Feature
AutoDoc now includes a Watch Mode that automatically monitors directories for file drops and generates documents when matching text and template files are detected. See [WATCH_MODE.md](WATCH_MODE.md) for complete instructions.

**Quick Start:**
- Command line: `java -jar autodoc-v1.7.jar --watch`
- GUI: Click "Start Watch Mode" button
- Drop matching text (.txt) and template (.docx, .pptx, .xlsx) files into `profile/input/` or `mhdocuments/input/`
- Generated documents appear in corresponding output directories

To recompile:
1) Clone this repo on your machine.
2) Make any necessary edits to source code.
3) If edits were made, open compile.sh and update the version number.
4) Open a Terminal window, navigate to directory of local repo.
5) Type "sh compile.sh jar" on a command line to generate a new .jar file.
