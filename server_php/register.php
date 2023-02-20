<?php 
    include 'db.php'; 


    // 파라미터 2개 (email, pw)
    $email = $_POST['email']; 
    $pw = $_POST['pw']; 

    
    $암호화pw = password_hash($pw, PASSWORD_DEFAULT); //비번암호화
    date_default_timezone_set('Asia/Seoul'); //date함수 사용전 에러방지
    $date = date('Y/m/d h:i:s a', time());


    $sql="INSERT INTO User(userEmail,userPw,createDate) 
            VALUES('$email', '$암호화pw', '$date')";
    
    if(mysqli_query($db,$sql)){ //쿼리받아온걸 if문안에 넣어서 true라면 원하는 코드를 실행함
        echo json_encode(array("response" => "true", "message" => "INSERT 완료"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        
    } else {
        echo json_encode(array("response" => "false", "message" => $num), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>