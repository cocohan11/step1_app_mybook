<?php 
    include 'db.php'; 
    // 서재 책 삭제

    // 파라미터 2개 (email, isbn)
    $email = $_POST['email']; 
    $isbn = $_POST['isbn'];


    //DELETE a book to library
    $sql="DELETE FROM Library WHERE userEmail = '$email' AND isbn = '$isbn'";


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

