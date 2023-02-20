<?php 
    include 'db.php'; 

    // 파라미터 2개 (email)
    $email = $_POST['email']; 
    
    $sqlMembers ="SELECT * FROM ClubMembers 
                where userEmail = '$email'"; 
    $resMembers =mysqli_query($db,$sqlMembers ); 
    $num = mysqli_num_rows($resMembers );

    $모임들 = array();
    while($rowMembers = mysqli_fetch_array($resMembers)){ //row 한 줄 한 줄 반복해서 배열에 담는다.
    

        $id = $rowMembers['ClubKey'];


        $sql="SELECT * FROM Club 
                where id = '$id' and turnout > 0;";
        $res=mysqli_query($db,$sql); 
        $row = mysqli_fetch_array($res);

        $sql_lastChat = "SELECT * FROM Chat 
                        WHERE id = (SELECT MAX(ID) FROM Chat where ClubKey = $id);";
        $res_lastChat=mysqli_query($db,$sql_lastChat); 
        $row_lastChat = mysqli_fetch_array($res_lastChat);


        
        array_push($모임들, array('id'=>$row['id'],
                                    'name'=>$row['name'],
                                    'imageUrl'=>$row['imageUrl'],
                                    'turnout'=>$row['turnout'],
                                    'fixed_num'=>$row['fixed_num'],
                                    'introduction'=>$row_lastChat['chat']
                                    // 'introduction'=>$row_lastChat['chat']
                                    // 채팅내용 추가예정
                                    //
                                ));
    }

    if($num > 0) { // 성공
        echo json_encode($모임들, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);

    } else { // 실패 또는 값 없음
        // echo json_encode(array("response" => false, "message"=> "0개또는null"), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>