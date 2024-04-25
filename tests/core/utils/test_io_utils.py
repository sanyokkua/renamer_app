from core.utils.io_utils import get_parent_folders


def test_empty_path():
    assert get_parent_folders("") == []


def test_none_path():
    assert get_parent_folders(None) == []


def test_single_folder():
    folder_path = "/folder"
    expected_result = []
    assert get_parent_folders(folder_path) == expected_result


def test_multiple_slashes():
    folder_path = "/user/documents//projects/"
    expected_result = ['user', 'documents']
    assert get_parent_folders(folder_path) == expected_result


def test_file_path():
    file_path = "/user/documents/projects/file.txt"
    expected_result = ['user', 'documents', 'projects']
    assert get_parent_folders(file_path) == expected_result


def test_folder_path():
    folder_path = "/user/documents/code_folder"
    expected_result = ['user', 'documents']
    assert get_parent_folders(folder_path) == expected_result


def test_root_path():
    root_path = "/"
    expected_result = []
    assert get_parent_folders(root_path) == expected_result


def test_root_path_win():
    root_path = "c:"
    expected_result = []
    assert get_parent_folders(root_path) == expected_result


def test_windows_path():
    windows_path = r"C:\Users\Documents\Projects\File.txt"
    expected_result = ['Users', 'Documents', 'Projects']
    assert get_parent_folders(windows_path) == expected_result


def test_windows_path_folder():
    windows_path = r"C:\Users\Documents\Projects\File_Folder\last_folder_to_not_include"
    expected_result = ['Users', 'Documents', 'Projects', 'File_Folder']
    assert get_parent_folders(windows_path) == expected_result
