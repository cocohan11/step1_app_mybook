<?php 
    include 'db.php'; 


    // 파라미터 7개 (email, title, cover, bookState, startDate, finishDate, rating)
    // (이멜, 제목, 표지, 책 상태(read/reading/hopeToRead), 독서시작, 독서종료, 평점)
    $email = $_POST['email']; 
    $title = $_POST['title']; 
    $isbn = $_POST['isbn']; 
    $author = $_POST['author']; 
    $cover = $_POST['cover']; 
    $bookState = $_POST['bookState']; 
    $startDate = $_POST['startDate']; 
    $finishDate = $_POST['finishDate']; 
    $rating = $_POST['rating']; 


    $title = str_replace ("'", "\'", $title);

    if($bookState=="read") {

        //INSERT a book to library
        $sql = "INSERT INTO Library(userEmail, title, isbn, author, cover, bookState, startDate, finishDate, rating)
        VALUES('$email', '$title', '$isbn', '$author', '$cover', '$bookState', '$startDate', '$finishDate', '$rating')";
 
    } else if($bookState=="reading"){

        $sql = "INSERT INTO Library(userEmail, title, isbn, author, cover, bookState, startDate, finishDate, rating)
        VALUES('$email', '$title', '$isbn', '$author', '$cover', '$bookState', '$startDate', null, null)";

    } else {
        
        $sql = "INSERT INTO Library(userEmail, title, isbn, author, cover, bookState, startDate, finishDate, rating)
        VALUES('$email', '$title', '$isbn', '$author', '$cover', '$bookState', null, null, null)";

    }



    //db update 성공
    if(mysqli_query($db,$sql)){

        echo json_encode(array("response" => "true"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }
    else //db update실패
    {
        echo json_encode(array("response" => $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>

