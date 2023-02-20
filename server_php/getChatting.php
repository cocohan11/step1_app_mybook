<?php 
    include 'db.php'; 
    // 파라미터 1개
    $id = $_POST['id']; 
    $email = $_POST['email']; 
    $index = $_POST['index']; // 안드에서 값 보내기
    $howMany = 20;

    // maxId 알아내기
    $sql_maxId = "SELECT MAX(id)
                    FROM Chat
                    WHERE entryPoint = '입장' and email = '$email' and ClubKey = $id;";
    $res_maxId=mysqli_query($db,$sql_maxId); 
    $row_maxId = mysqli_fetch_array($res_maxId);
    $maxId = $row_maxId['MAX(id)'];


    // null 에러남. 채팅 싹 삭제하는 경우. 
    if($maxId == null) {
        $sql_update="UPDATE Chat SET entryPoint = '입장' where ClubKey = '$id' and email = '$email' "; 
        if(mysqli_query($db,$sql_update)) {
            $res_maxId=mysqli_query($db,$sql_maxId); 
            $row_maxId = mysqli_fetch_array($res_maxId);
            $maxId = $row_maxId['MAX(id)'];
        }
    }


    // $채팅들 = array();
    // echo json_encode($채팅들, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);

    // 채팅들 가져오기
    $sql="SELECT C.ClubKey, C.chat, C.email, U.userImg, U.userName
            FROM User AS U 
            JOIN Chat AS C
            ON U.userEmail = C.email 
            WHERE C.ClubKey = '$id' and C.id >= $maxId
            ORDER BY C.id DESC
            LIMIT $index, $howMany;";


    $res=mysqli_query($db,$sql); 
    $num = mysqli_num_rows($res);
    $채팅들 = array();


    while($row = mysqli_fetch_array($res)){ //row 한 줄 한 줄 반복해서 배열에 담는다.
    array_push($채팅들, array(
                                'clubNum'=>$row['ClubKey'],
                                'chat'=>$row['chat'],
                                'email'=>$row['email'],
                                'img'=>$row['userImg'],
                                'name'=>$row['userName'],
                                'purpose'=>$sql
                                // 'purpose'=>"채팅"
                                // 목적, 이름, 이미지는 클라이언트 array에 있음. 합치기
                            ));
    }

 
    if($num > 0) { // 성공
        // $채팅들 = array_reverse($채팅들); // 배열 순서 반대로 뒤집기
        echo json_encode($채팅들, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);

    } else { // 실패 또는 값 없음
        echo json_encode(array("response" => false, "message"=> $maxId), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }
    mysqli_close($db);  
?>