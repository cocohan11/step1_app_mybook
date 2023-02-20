<?php 
    include 'db.php'; 
    // 모임에서 나가기

    // 파라미터 2개 (email, isbn)
    $email = $_POST['email']; 
    $id = $_POST['id'];


    //DELETE a member to ClubMembers
    $sql="DELETE FROM ClubMembers WHERE userEmail = '$email' AND ClubKey = '$id'";
    if(mysqli_query($db,$sql)){


        // update turnout --; 
        $sql_update="UPDATE Club SET turnout = turnout-1 where id = '$id' and turnout > 0;"; 
        if(mysqli_query($db,$sql_update)){
            echo json_encode(array("response" => true, "message"=> $sql_update), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        }
    }
    else //db update실패
    {
        echo json_encode(array("response" => $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>