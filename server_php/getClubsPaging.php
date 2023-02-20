<?php 
    include 'db.php'; 

    // 파라미터 2개 
    $purpose = $_POST['purpose']; 
    $index = $_POST['index']; 
    $howMany = 5;


    if($purpose == "new") { // 신규모임
        $sql=" SELECT * FROM Club 
                where turnout < fixed_num and turnout != 0
                ORDER BY id DESC
                LIMIT $index, $howMany; "; // 정원이 남은 모임만 조회, 최신 순

    } else if($purpose == "asc") {
        $sql=" SELECT * FROM Club 
                where turnout < fixed_num and turnout != 0
                ORDER BY start_date ASC
                LIMIT $index, $howMany; "; // 오름차순. 빨리 시작하는 순
    }


    $result=mysqli_query($db,$sql); 
    $num = mysqli_num_rows($result);
    $모임들 = array();
    while($row = mysqli_fetch_array($result)){ //row 한 줄 한 줄 반복해서 배열에 담는다.
        
        $master_email = $row['master_email'];
        $sqlUser="  SELECT * FROM User
                    where userEmail = '$master_email'; "; 
        $resUser=mysqli_query($db,$sqlUser); 
        $rowUser = mysqli_fetch_array($resUser);
        array_push($모임들, array(
                                    'id'=>$row['id'],
                                    'name'=>$row['name'],
                                    'imageUrl'=>$row['imageUrl'],
                                    'bookTitle'=>$row['bookTitle'],
                                    'introduction'=>$row['introduction'],
                                    'turnout'=>$row['turnout'],
                                    'fixed_num'=>$row['fixed_num'],
                                    'ages'=>$row['ages'],
                                    'theme'=>$row['theme'],
                                    'start_date'=>$row['start_date'],
                                    'finish_date'=>$row['finish_date'],
                                    'master_img'=>$rowUser['userImg'],
                                    'master_name'=>$rowUser['userName']
                                ));
    }
    if($num > 0) { // 성공
        echo json_encode($모임들, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        // echo json_encode(array($모임들, "response" => true, "message"=> $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        // echo json_encode(array("response" => true, "message"=> $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);

    } else { // 실패 또는 값 없음
        echo json_encode(array("response" => false, "message"=> $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    }
    mysqli_close($db);  
?>