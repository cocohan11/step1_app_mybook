<?php 
    include 'db.php'; 

    // 파라미터 2개 
    $email = $_POST['email']; 
    $isbn = $_POST['isbn']; 
    $index = $_POST['index']; 
    $howMany = 10;


    $sql="SELECT N.page, N.imageUrl, N.note, N.Date, N.open, N.id
            FROM myBook.Library AS L
            JOIN myBook.Note AS N
            ON N.library_id = L.id
            WHERE L.userEmail = '$email' and isbn = '$isbn' 
            ORDER BY N.Date DESC
            LIMIT $index,$howMany; "; // 메모갯수값 알아내기


    $result=mysqli_query($db,$sql); 
    $num = mysqli_num_rows($result);
    


    $책정보 = array();
    while($row = mysqli_fetch_array($result)){ //row 한 줄 한 줄 반복해서 배열에 담는다.
    
        // 0,1로 응답하니까 안드에서 인식을 못함
        $open = false;
        if($row['open'] == 0) {
            $open = false;
        } else {
            $open = true;
        }


        //6개 (title, cover, Date, rating)
        array_push($책정보, array('id'=>$row['id'],
                                    'page'=>$row['page'],
                                    'imgUrl'=>$row['imageUrl'],
                                    'content'=>$row['note'],
                                    'date'=>$row['Date'],
                                    'isbn'=>$isbn,
                                    'open'=> $open));
    }

    if($num > 0) { // 성공
        echo json_encode($책정보, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    
    } else { // 실패 또는 값 없음
        array_push($책정보, array("response" => $sql)); 
        echo json_encode($책정보, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>