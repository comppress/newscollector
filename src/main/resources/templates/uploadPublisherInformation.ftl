<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title></title>
</head>
<body>
<div>
    Upload File for Publisher Information. Format is CSV. <br>
</div>
<div>
    <form action="/receivePublisherInformation" method="post" enctype="multipart/form-data">
        Select File to upload:
        <input type="file" name="fileToUpload" id="fileToUpload">
        <input type="submit" value="Upload CSV" name="submit">
    </form>
</div>
<br>
</body>
</html>