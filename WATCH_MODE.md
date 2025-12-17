# AutoDoc Watch Mode

## Overview

AutoDoc now supports a Watch Mode that automatically monitors directories for file drops and generates documents when matching text and template files are detected.

## Directory Structure

The watch mode monitors two main directories:

```
autodoc/
├── profile/
│   ├── input/      # Drop files here
│   └── output/     # Generated documents appear here
└── mhdocuments/
    ├── input/      # Drop files here
    └── output/     # Generated documents appear here
```

## How It Works

1. **Drop Files**: Place a text file (.txt) and a template file (.docx, .pptx, or .xlsx) with matching base names into either `profile/input/` or `mhdocuments/input/` directory.

   Example:
   - `profile/input/report.txt`
   - `profile/input/report.docx`

2. **Automatic Processing**: The watch mode automatically detects the files, matches them by name, and generates the output document.

3. **Output**: The generated document appears in the corresponding output directory:
   - `profile/output/report.docx`

4. **File Organization**: After processing, the input files are moved to a `processed/` subdirectory within the input folder.

## Usage

### Command Line Mode

Start the watch mode from the command line:

```bash
java -jar autodoc-v1.7.jar --watch
```

The application will:
- Create the required directory structure if it doesn't exist
- Start monitoring both `profile/input/` and `mhdocuments/input/` directories
- Display status messages as files are detected and processed
- Continue running until you press Ctrl+C

### GUI Mode

1. Launch AutoDoc normally (double-click the JAR or run without arguments)
2. Click the "Start Watch Mode" button at the bottom of the window
3. A dialog will appear confirming the directories being monitored
4. Drop files into the input directories
5. Click "Stop Watch Mode" when done

## File Naming Requirements

- **Text files** must have a `.txt` extension
- **Template files** must have `.docx`, `.pptx`, or `.xlsx` extensions
- **Base names must match** (e.g., `report.txt` matches with `report.docx`)
- The extension determines the output file type

## Examples

### Example 1: Word Document

Files:
- `profile/input/meeting_notes.txt`
- `profile/input/meeting_notes.docx`

Result:
- `profile/output/meeting_notes.docx`

### Example 2: PowerPoint Presentation

Files:
- `mhdocuments/input/presentation.txt`
- `mhdocuments/input/presentation.pptx`

Result:
- `mhdocuments/output/presentation.pptx`

### Example 3: Excel Spreadsheet

Files:
- `profile/input/data_report.txt`
- `profile/input/data_report.xlsx`

Result:
- `profile/output/data_report.xlsx`

## Processing Details

- Files must be stable (not being written) for at least 2 seconds before processing
- The watch mode checks for matching files every second
- Both text and template files must be present before processing begins
- After successful processing, input files are moved to `processed/` subdirectory

## Text File Format

Text files should follow the standard AutoDoc format:

```
${keyword1}    value1
${keyword2}    value2
# Comments start with #
${keyword3}    null
```

See the main AutoDoc documentation for complete text file formatting rules.

## Troubleshooting

**Files not being processed:**
- Ensure file names match exactly (except for extension)
- Check that both text and template files are present
- Wait a few seconds for file detection
- Check console output for error messages

**Watch mode not starting:**
- Ensure you have write permissions in the autodoc directory
- Check that directories can be created

**Generated files not appearing:**
- Check the output directory
- Look for error messages in the console
- Verify that template file has the correct keywords

## Notes

- Multiple file pairs can be processed simultaneously
- The watch mode runs in separate threads and doesn't block the GUI
- Processed files are archived in the `processed/` subdirectory for reference
- You can manually move files back from `processed/` to re-process them
