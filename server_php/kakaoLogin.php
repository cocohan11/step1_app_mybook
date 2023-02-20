<?php 
    include 'db.php'; 
    // 카카오로 로그인 (이멜 중복되는지만 확인)


    // 파라미터 1개 (email)
    $email = $_POST['email']; 
    $ImgUrl = $_POST['ImgUrl']; 

    
    // 이메일중복체크
    $sql="SELECT * from User 
            where userEmail = '$email'"; // 이메일 중복확인
    $result=mysqli_query($db,$sql); //접속해서 받아온 값
    $num = mysqli_num_rows($result);

    if ($num == 1) { // 로그인하기
        echo json_encode(array("response" => "true", "message" => "로그인하기"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
            
    } else if ($num == 0) { // 회원가입하기(DB)
        
        date_default_timezone_set('Asia/Seoul'); //date함수 사용전 에러방지
        $date = date('Y/m/d h:i:s a', time());
        
        $sql="INSERT INTO User(userEmail, userPw, userImg, createDate) 
                VALUES('$email', null, '$ImgUrl', '$date')";


        if(mysqli_query($db,$sql)){ 
             echo json_encode(array("response" => "true", "message" => "없는 이메일입니다. 회원가입하기"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
            
        } else {
            echo json_encode(array("response" => "false", "message" => "없는 이메일입니다. 회원가입실패했습니다."), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        }

    } else {
        echo json_encode(array("response" => "false", "message" => "조회결과 0~1개가 아님"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>