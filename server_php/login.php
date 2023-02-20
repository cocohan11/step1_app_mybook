<?php 
    include 'db.php'; 


    // 파라미터 2개 (email, pw)
    $email = $_POST['email']; 
    $pw = $_POST['pw']; 


    $sql="SELECT * from User 
            where userEmail = '$email'"; 
    $result=mysqli_query($db,$sql); 
    $row = mysqli_fetch_array($result);
    $hash = $row['userPw']; // 해싱되어 db에 저장된 pw
    

    $pw일치 = password_verify($pw, $hash); // password_hash()로 암호화한 비밀번호가 사용자가 입력한 값과 같은지 확인하는 함수

    
    if($pw일치){ // 비번 일치한다. 로그인 하기
        echo json_encode(array("response" => $pw일치, "message" => "pw일치한다"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        
    } else {
        echo json_encode(array("response" => $pw일치, "message" => "pw일치X"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>