<?php 
    include 'db.php'; 
    // 서재 책 수정

    // 파라미터 7개 (email, title, cover, bookState, startDate, finishDate, rating)
    // (이멜, 제목, 표지, 책 상태(read/reading/hopeToRead), 독서시작, 독서종료, 평점)
    $email = $_POST['email']; 
    $isbn = $_POST['isbn']; // 선별

    $bookState = $_POST['bookState']; 
    $startDate = $_POST['startDate']; 
    $finishDate = $_POST['finishDate']; 
    $rating = $_POST['rating']; 


    $title = str_replace ("'", "\'", $title);

    if($bookState=="read") { // 읽은 책

        //UPDATE a book to library
        $sql="UPDATE Library 
                SET bookState = '$bookState', startDate = '$startDate', finishDate ='$finishDate', rating ='$rating'
                where userEmail = '$email' AND isbn = '$isbn' "; // error! where절에는 콤마대신 and를 썼어야 됐음
 
    } else if($bookState=="reading"){ // 읽고있는 책

        $sql="UPDATE Library 
                SET bookState = '$bookState', startDate = '$startDate', finishDate =null, rating = null
                where userEmail = '$email' AND isbn = '$isbn' "; 

    } else { // 읽고싶은 책
        
        $sql="UPDATE Library 
        SET bookState = '$bookState', startDate = null, finishDate =null, rating = null
        where userEmail = '$email' AND isbn = '$isbn' "; 

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

