<?php 
    include 'db.php'; 
    // 닉넴만 바꿀 때


    // 파라미터 3개 (email, pw, name)
    $email = $_POST['email']; 
    $name = $_POST['name']; 

   
    //pw자리에 update하기
    $sql="UPDATE User SET userName = '$name' where userEmail = '$email' "; // 닉넴, (이미지 주소)업뎃

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

