<?php 
    include 'db.php'; 
    // 내 정보 - 닉넴, 프사


    // 파라미터 1개 (email)
    $email = $_POST['email']; 

    
    // 닉넴, 프사
    $sql="SELECT userName, userImg from User where userEmail = '$email'";

    $result=mysqli_query($db,$sql); //접속해서 받아온 값
    $num = mysqli_num_rows($result);
    $row = mysqli_fetch_array($result);
    $userName = $row['userName']; 
    $userImg = $row['userImg']; 
    

    // 책 권수
    $sql책권수="SELECT * FROM Library where userEmail = '$email'";
    $result책권수=mysqli_query($db,$sql책권수); 
    $num책권수 = mysqli_num_rows($result책권수);


    if ($num == 1) { // 통과O
        echo json_encode(array("response" => "true",  "message" => $sql, "userName" => $userName, "userImg" => $userImg, "countMyBook" => $num책권수), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
            
    } else { // 통과X
        echo json_encode(array("response" => "false", "message" => "select 1개가 아님"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }

    mysqli_close($db);  
?>