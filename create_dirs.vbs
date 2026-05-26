' VBScript to create Android project directory structure
' This script can be run with: cscript create_dirs.vbs

Set fso = CreateObject("Scripting.FileSystemObject")
baseDir = "H:\Projects\agents-android-app-development"

directories = Array( _
    "app\src\main\java\com\example\boardgamescoretracker\ui\screens", _
    "app\src\main\java\com\example\boardgamescoretracker\ui\theme", _
    "app\src\main\java\com\example\boardgamescoretracker\data\db", _
    "app\src\main\java\com\example\boardgamescoretracker\data\repository", _
    "app\src\main\java\com\example\boardgamescoretracker\viewmodel", _
    "app\src\main\res\values", _
    "app\src\main\res\drawable", _
    "app\src\test\java\com\example\boardgamescoretracker", _
    "app\src\androidTest\java\com\example\boardgamescoretracker" _
)

For Each dir In directories
    fullPath = fso.BuildPath(baseDir, dir)
    If Not fso.FolderExists(fullPath) Then
        fso.CreateFolder(fullPath)
        WScript.Echo "Created: " & dir
    Else
        WScript.Echo "Already exists: " & dir
    End If
Next

' Verify by listing the app directory
WScript.Echo ""
WScript.Echo "Verifying directory structure..."
WScript.Echo "====================================="

appPath = fso.BuildPath(baseDir, "app")
If fso.FolderExists(appPath) Then
    WScript.Echo "app directory exists ✓"
    ListFolders fso.GetFolder(appPath), ""
Else
    WScript.Echo "ERROR: app directory was not created!"
End If

Sub ListFolders(folder, indent)
    For Each subfolder In folder.SubFolders
        WScript.Echo indent & subfolder.Name & "\"
        ListFolders subfolder, indent & "  "
    Next
End Sub
