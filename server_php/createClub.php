<?php 
    include 'db.php'; 
    // 모임 생성

    // 파라미터 13개 
    $master_email = $_POST['master_email']; 
    $isbn = $_POST['isbn'];
    $name = $_POST['name'];
    $introduction = $_POST['introduction'];
    $imageUrl = $_POST['imageUrl'];
    $bookTitle = $_POST['bookTitle'];
    $turnout = $_POST['turnout'];
    $fixed_num = $_POST['fixed_num'];
    $ages = $_POST['ages'];
    $theme = $_POST['theme'];
    $term = $_POST['term'];
    $open_date = $_POST['open_date'];
    $start_date = $_POST['start_date'];
    $finish_date = $_POST['finish_date'];



    $sql = "INSERT INTO Club (master_email, isbn, name, introduction, imageUrl, bookTitle, turnout, fixed_num, ages, theme, term, open_date, start_date, finish_date) 
            VALUES('$master_email', '$isbn', '$name', '$introduction', '$imageUrl', '$bookTitle', '$turnout', $fixed_num, '$ages', '$theme', $term, '$open_date', '$start_date', '$finish_date')";        
   
   
   $result=mysqli_query($db,$sql); 
    if($result){

        $row = mysqli_fetch_array($result);
        $ClubTableId = mysqli_insert_id($db);


        // 방장은 php파일에서 db에 멤버추가 됨
        // 나머지 멤버들은 
        $sql = "INSERT INTO ClubMembers (ClubKey, userEmail, master) 
        VALUES('$ClubTableId', '$master_email', 1)";        

        if(mysqli_query($db,$sql)){
            echo json_encode(array("response" => true, "message"=> $ClubTableId), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(array("response" => false, "message"=> "dd".$sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        }
    } else {
        echo json_encode(array("response" => false, "message"=> $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>




                    