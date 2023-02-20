<?php 
    include 'db.php'; //비밀번호 보호차원에서 include로 불러옴
    // 회원가입 시 이메일 중복검사


    // 파라미터 1개 (email)
    $email = $_POST['email']; 

    
    // 이메일중복체크
    $sql="SELECT * from User 
            where userEmail = '$email'"; // 이메일 중복확인
    $result=mysqli_query($db,$sql); //접속해서 받아온 값
    $num = mysqli_num_rows($result);

    if ($num == 0) { // 통과O
        echo json_encode(array("response" => "true", "message" => "SELECT 완료"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
            
    } else { // 통과X
        echo json_encode(array("response" => "false", "message" => "이미 가입된 이메일입니다."), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }

    mysqli_close($db);  
?>