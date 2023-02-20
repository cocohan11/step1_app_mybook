<?php 
    include 'db.php'; 

    // 파라미터 1개
    $email = $_POST['email']; 


    $sql="SELECT L.id, L.cover, L.title, L.rating, L.isbn, max(N.Date), COUNT(library_id)
            FROM myBook.Library AS L
            JOIN myBook.Note AS N
            ON L.id = N.library_id
            WHERE L.userEmail = '$email'
            GROUP BY L.id
            ORDER BY max(N.Date) DESC; "; // 메모갯수값 알아내기



    $result=mysqli_query($db,$sql); 
    $num = mysqli_num_rows($result);
    


    $책정보 = array();
    while($row = mysqli_fetch_array($result)){ //row 한 줄 한 줄 반복해서 배열에 담는다.
    
        //6개 (title, cover, Date, rating)
        array_push($책정보, array('title'=>$row['title'],
                                    'cover'=>$row['cover'],
                                    'date'=>$row['max(N.Date)'],
                                    'isbn'=>$row['isbn'],
                                    'countNote'=>$row['COUNT(library_id)'],
                                    'rating'=>$row['rating'] ));
    }

    if($num > 0) { // 성공
        echo json_encode($책정보, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    
    } else { // 실패 또는 값 없음
        array_push($책정보, array("response" => $sql)); 
        echo json_encode($책정보, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }


    mysqli_close($db);  
?>