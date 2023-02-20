<?php 
    include 'db.php'; //비밀번호 보호차원에서 include로 불러옴


    // 파라미터 1개 (email)
    $email = $_POST['email']; 


    //탈퇴
    $sql="DELETE FROM User WHERE userEmail = '$email' ";
    
    if(mysqli_query($db,$sql)){ // 회원정보 영구삭제

        echo json_encode(array("response" => "true"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }
    else //db DELETE 실패
    {
        echo json_encode(array("response" => $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  

?>