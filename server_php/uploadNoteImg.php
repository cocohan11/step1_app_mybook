<?php 
    include 'db.php'; //비밀번호 보호차원에서 include로 불러옴
    // 노트작성
    // pram (email, isbn, page, note, open, body)


    // from android
    $email = preg_replace("/[\"\']/i", "", $_POST['email']); 
    $isbn = preg_replace("/[\"\']/i", "", $_POST['isbn']); 
    $note = preg_replace("/[\"\']/i", "", $_POST['note']); 
    $update_id = $_POST['id']; 
    $open = $_POST['open']; 
    $page = $_POST['page']; 
    date_default_timezone_set('Asia/Seoul');
    $thisDate = date("Y-m-d H:i:s");


    // 전송된 사진이 있다면
    $fileName = $_FILES['uploaded_file']['name'];
    $imgUrl특수문자 = 'http://15.164.129.103/myNoteOfBook/'.$fileName; //내가 카톡처럼 사진띄우는 웹페이지를 만듦
    $imgUrl = preg_replace("/[\"\']/i", "", $imgUrl특수문자); 
        
    // 사진파일 저장
    $basename = basename($_FILES['uploaded_file']['name']); 
    $임시파일 = $_FILES['uploaded_file']['tmp_name']; //임시저장파일의 이름
    
    // $따옴표제거임시파일 = preg_replace("/[ #\&\+\-%@=\/\\\:;,\.'\"\^`~\_|\!\?\*$#<>()\[\]\{\}]/i", "", $임시파일);
    $uploads_dir="../myNoteOfBook/".$basename; //저장할 폴더위치 ../han.jpg
    $에러 = $_FILES["uploaded_file"]["error"];


    // 이미지 파일 있다면
    if($임시파일 != null) {
        if(move_uploaded_file($임시파일, $uploads_dir)) { //위치변경

            // Library table - email과 isbn으로 id값 알아내기
            // Note tabele - id값이 외래키. 중복 가능. 한 책에 대한 여러개의 노트 
            // (정렬 - 최신순/페이지순)
            // get 'id'
            $sql="SELECT id FROM Library where userEmail = '$email' and isbn = '$isbn' "; 
            $result=mysqli_query($db,$sql); 
            $num = mysqli_num_rows($result);
            $row = mysqli_fetch_array($result);
            $id = $row['id'];
    
            if ($num == 1) { // 통과O
                if($update_id != 0) {
                    $sql="UPDATE Note 
                            SET page = '$page', note = '$note', open = $open, imageUrl = '$imgUrl'
                            where id = '$update_id';"; // error! and 대신 콤마를 썼어야 됐음


                } else{
                    // insert this note
                    $sql = "INSERT INTO Note (library_id, page, note, open, imageUrl, Date) 
                            VALUES('$id', '$page', '$note', $open, '$imgUrl', '$thisDate')";        
                }

                // success2
                if(mysqli_query($db,$sql)){
                    echo json_encode(array("response" => true, "message"=> $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
                }
                    
            } else { // 통과X
                echo json_encode(array("response" => false), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
            }
    
    
        
        } else { //파일 너무 큼(8MB 이상)
            // $response = 'false_사진8BM이상';
            // $response = 'false 파일저장위치변경 : '.$임시파일; // \/tmp\/phpoufQyF
            // $response = "false 베스이네임 : ".$basename; // summer.jpg
            // $response = "false uploads_dir : ".$uploads_dir; // ..\/profImgOfBook\/summer.jpg
            $response = "false 임시파일 : ".$임시파일." 에러 : ".$에러; // tmpphp2JzYyF, 낫띵
            echo json_encode(array("response" => $response, "message"=> $imgUrl), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        }

    // 이미지 파일 없다면
    } else {
        // get 'id'
        $sql="SELECT id FROM Library where userEmail = '$email' and isbn = '$isbn' "; 
        $result=mysqli_query($db,$sql); 
        $num = mysqli_num_rows($result);
        $row = mysqli_fetch_array($result);
        $id = $row['id'];

        if ($num == 1) { // 통과O


            if($update_id != 0) {
            
                $sql="UPDATE Note 
                        SET page = '$page', note = '$note', open = $open
                        where id = '$update_id';"; // error! and 대신 콤마를 썼어야 됐음

            } else{ // insert this note
                $sql = "INSERT INTO Note (library_id, page, note, open, Date) 
                    VALUES('$id', '$page', '$note', $open, '$thisDate')";     
            }


            if(mysqli_query($db,$sql)){
                echo json_encode(array("response" => true, "message"=> $sql), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
            }
                
        } else { // 통과X   
            echo json_encode(array("response" => false), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        }
    }

    
    // 에러 "JSON document was not fully consumed."
    // 에러난 이유 : json_encode를 여러번 사용해서 얘가 json이 여러개 못 보낸거였음. 최종값 하나만 보내기.
    
    //파일을 안드로이드에 보낼 때 어떻게 보내지? url로 보는건가?
    //나의공간 만들 때 다시 여기서 코드작성하자

    mysqli_close($db);  
?>