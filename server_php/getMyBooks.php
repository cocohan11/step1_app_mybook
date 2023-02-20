<?php 
    include 'db.php'; 

    // 파라미터 2개 (email, bookState)
    $email = $_POST['email']; 
    $bookState = $_POST['bookState']; 


    // 책 상태별 결과값 응답
    if($bookState=="read" || $bookState=="reading" || $bookState=="hopeToRead") {

        $sql="SELECT * FROM Library where userEmail = '$email' and bookState ='$bookState' ORDER BY id DESC"; 
        $result=mysqli_query($db,$sql); 
        $num = mysqli_num_rows($result);
        
    // 모든 책 응답
    } else {

        $sql="SELECT * FROM Library where userEmail = '$email' ORDER BY id DESC"; // 내림차순
        $result=mysqli_query($db,$sql); 
        $num = mysqli_num_rows($result);
        
    }

   

    $책정보 = array();
    while($row = mysqli_fetch_array($result)){ //row 한 줄 한 줄 반복해서 배열에 담는다.
    
        //6개 (title, cover, bookState, startDate, finishDate, rating)
        array_push($책정보, array('title'=>$row['title'],
                                    'isbn'=>$row['isbn'],
                                    'author'=>$row['author'],
                                    'cover'=>$row['cover'],
                                    'bookState'=>$row['bookState'],
                                    'startDate'=>$row['startDate'],
                                    'finishDate'=>$row['finishDate'],
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