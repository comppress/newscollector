<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title></title>
</head>
<body>
<div>
    <form action="/receiveCSV" method="post" enctype="multipart/form-data">
        Select RssFeeds in CSV Format to upload:
        <input type="file" name="fileToUpload" id="fileToUpload">
        <input type="submit" value="Upload CSV" name="submit">
    </form>
</div>
<br>
</body>
</html>