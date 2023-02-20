<?php 
    include 'db.php'; 


    // 파라미터 2개 (email, pw)
    $email = $_POST['email']; 
    $pw = $_POST['pw']; // 새로 받은 변경할 비밀번호
    $해시pw = password_hash($pw, PASSWORD_DEFAULT); 

   
    //pw자리에 update하기
    $sql="UPDATE User SET userPw = '$해시pw' where userEmail = '$email' "; //insert가 아니라 update를 해야했음

    //db update 성공
    if(mysqli_query($db,$sql)){

        echo json_encode(array("response" => "true"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }
    else //db update실패
    {
        echo json_encode(array("response" => "false"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>


