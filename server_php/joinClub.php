<?php 
    include 'db.php'; 
    // 모임 가입
    // 파라미터 2개 
    $email = $_POST['email'];
    $ClubKey = $_POST['id'];


    $sql = "INSERT INTO ClubMembers (ClubKey, userEmail, master) 
            VALUES('$ClubKey', '$email', 0)";        
    if(mysqli_query($db,$sql)){


        // update turnout ++; 
        $sql_update="UPDATE Club SET turnout = turnout+1 where id = '$ClubKey' and turnout < fixed_num"; 
        if(mysqli_query($db,$sql_update)){
            echo json_encode(array("response" => true, "message"=> $ClubKey), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        }
    } else {
        echo json_encode(array("response" => false, "message"=> "dd".$sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>
                    