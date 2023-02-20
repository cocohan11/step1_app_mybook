<?php 
    include 'db.php'; 
    // 노트 삭제

    // 파라미터 2개 (email, id)
    $email = $_POST['email']; 
    $id = $_POST['id'];


    //DELETE a book to library
    $sql="DELETE FROM Note WHERE id = $id";


    //db update 성공
    if(mysqli_query($db,$sql))
    {
        echo json_encode(array("response" => "true"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }
    else //db update실패
    {
        echo json_encode(array("response" => $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>

