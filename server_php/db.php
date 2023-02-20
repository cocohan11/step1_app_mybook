<?php 

    $db = mysqli_connect('iamhere.cdf5mmjsw63q.ap-northeast-2.rds.amazonaws.com', 'han', 'jo811275', 'myBook');
    if (!$db) //db접속되지 않는다면 echo로 상태알려줌
    {  
        echo "MySQL 접속 에러 : ";
        echo mysqli_connect_error();
        exit();  
    }  
    mysqli_set_charset($db,"utf8"); //한글안깨지려고?
?>