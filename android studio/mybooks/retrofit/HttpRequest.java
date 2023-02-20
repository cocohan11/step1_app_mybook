package com.example.mybooks.retrofit;

import com.example.mybooks.model.AladinBook;
import com.example.mybooks.model.ClientInfo;
import com.example.mybooks.model.Club;
import com.example.mybooks.model.Note;
import com.example.mybooks.model.Response;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface HttpRequest {

    @FormUrlEncoded //POST방식 사용시 입력해야함
    @POST("myBook/register.php") // URL주소의 BaseDomain이하의 주소를 입력
    Call<Response> getRegister( // Call<반환타입> , 파라미터:php로 보내는 데이터. php는 키값인 ""으로 구분함
                                @Field("email") String email, // php의 $_POST['JoinEmail']로 가는 부분
                                @Field("pw") String pw
    );


    @FormUrlEncoded // 가입된 이메일인가
    @POST("myBook/register_match.php")
    Call<Response> getRegister_match(
            @Field("email") String email
    );


    @FormUrlEncoded
    @POST("myBook/login.php")
    Call<Response> getLogin(
            @Field("email") String email,
            @Field("pw") String pw
    );


    @FormUrlEncoded
    @POST("myBook/changePw.php")
    Call<Response> getChangePw(
            @Field("email") String email,
            @Field("pw") String pw
    );


    @FormUrlEncoded
    @POST("myBook/deleteUser.php")
    Call<Response> getDeleteUser(
            @Field("email") String email
    );

    @FormUrlEncoded // 카카오 로그인 (네이티브와 별도 처리)
    @POST("myBook/kakaoLogin.php")
    Call<Response> getKakaoLogin(
            @Field("email") String email,
            @Field("ImgUrl") String ImgUrl
    );


    @FormUrlEncoded // 프로필 수정 - 닉넴만
    @POST("myBook/changeProfile.php")
    Call<Response> getChangeProfile(
            @Field("email") String email,
            @Field("name") String name
    );


    @Multipart // 프로필 수정 - 닉넴, 사진
    @POST("myBook/uploadProfileImg.php")
    Call<Response> getUploadProfileImg(
            @Part("email") String email,
            @Part("name") String name,
            @Part MultipartBody.Part file
    );


    @FormUrlEncoded // 내 정보 (닉넴, 프사)
    @POST("myBook/getProfile.php")
    Call<Response> getProfile(
            @Field("email") String email
    );

    @FormUrlEncoded // 책 등록
    @POST("myBook/saveBook.php")
    Call<Response> getSaveBook(
            @Field("email") String email,
            @Field("title") String title,
            @Field("isbn") String isbn,
            @Field("author") String author,
            @Field("cover") String cover,
            @Field("bookState") String bookState, // (read/reading/hopeToRead)
            @Field("startDate") Long startDate,
            @Field("finishDate") Long finishDate,
            @Field("rating") float rating
    );

    @FormUrlEncoded // 책 수정
    @POST("myBook/editBook.php")
    Call<Response> getEditBook(
            @Field("email") String email,
            @Field("isbn") String isbn,

            @Field("bookState") String bookState, // (read/reading/hopeToRead)
            @Field("startDate") Long startDate,
            @Field("finishDate") Long finishDate,
            @Field("rating") float rating
    );


//    @FormUrlEncoded // 내 정보 (닉넴, 프사)
//    @POST("myBook/getProfile.php")
//    Call<Response> getMyBooks(
//            @Field("email") String email
//    );

    @FormUrlEncoded
    @POST("myBook/getMyBooks.php") // 내 서재
    Call<ArrayList<AladinBook>> getMyLibrary( // isbn를 arraylist로 담아옴
          @Field("email") String email,
          @Field("bookState") String bookState
    );


    @FormUrlEncoded // 내 정보 (닉넴, 프사)
    @POST("myBook/deleteBook.php")
    Call<Response> getDeleteBook(
            @Field("email") String email,
            @Field("isbn") String isbn
    );


    @Multipart // 노트 작성
    @POST("myBook/uploadNoteImg.php")
    Call<Response> getUploadNoteImg(
            @Part("email") String email,
            @Part("isbn") String isbn,
            @Part("page") int page,
            @Part("note") String note,
            @Part("open") boolean open,
            @Part("id") int id,
            @Part MultipartBody.Part file
    );


    @FormUrlEncoded // 노트 삭제
    @POST("myBook/deleteNote.php")
    Call<Response> getDeleteNote(
            @Field("email") String email,
            @Field("id") int id
    );


    @FormUrlEncoded // 노트 첫 화면
    @POST("myBook/showBooksForNote.php")
    Call<ArrayList<AladinBook>> getBooksForNote(
            @Field("email") String email
    );


    @FormUrlEncoded // 노트 첫 화면
    @POST("myBook/getNotesAboutTheBook.php")
    Call<ArrayList<Note>> getNotesAboutTheBook( // note class
            @Field("email") String email,
            @Field("isbn") String isbn,
            @Field("index") int index
    );


    @FormUrlEncoded // 모임 생성
    @POST("myBook/createClub.php")
    Call<Response> getCreateClub(
            @Field("master_email") String master_email,
            @Field("isbn") String isbn,
            @Field("name") String name,
            @Field("introduction") String introduction,
            @Field("imageUrl") String imageUrl,
            @Field("bookTitle") String bookTitle,
            @Field("turnout") int turnout, // 참가자 수
            @Field("fixed_num") int fixed_num, // 고정 인원
            @Field("ages") String ages,
            @Field("theme") String theme,
            @Field("term") int term, // 1주 2주..
            @Field("open_date") Long open_date,
            @Field("start_date") Long start_date,
            @Field("finish_date") Long finish_date
    );


    @FormUrlEncoded
    @POST("myBook/getMyClubs.php") // 내 모임 조회
    Call<ArrayList<Club>> getMyClubs(
            @Field("email") String email
    );


    @FormUrlEncoded // 모임 생성
    @POST("myBook/joinClub.php")
    Call<Response> getJoinClub(
            @Field("email") String email,
            @Field("id") String id
    );


    @FormUrlEncoded // 모임 생성
    @POST("myBook/leaveClub.php")
    Call<Response> getLeaveClub(
            @Field("email") String email,
            @Field("id") String id
    );



    @FormUrlEncoded // 모임 채팅내역 불러오기
    @POST("myBook/getChatting.php")
    Call<ArrayList<ClientInfo>> getChatting(
            @Field("email") String email,
            @Field("id") String id,
            @Field("index") int index // 페이징
    );


/*

    @FormUrlEncoded
    @POST("myBook/getClubs.php")
    Call<ArrayList<Club>> getClubs(
            @Field("purpose") String purpose
    );

*/

    @FormUrlEncoded // 모임 페이징
    @POST("myBook/getClubsPaging.php")
    Call<ArrayList<Club>> getClubsPaging(
            @Field("purpose") String purpose, // 모임 조회(신규/시작순)
            @Field("index") int index // 5개씩
     );


    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ


    @GET("ItemSearch.aspx") // 알라딘 책검색 api
    Call<String> getSearchBook(
            @Query("ttbkey") String ttbkey, // 필수값 2개
            @Query("Query") String Query,
            @Query("Start") int Start
    );

    @GET("ItemLookUp.aspx") // 알라딘 책조회 api
    Call<String> getItemLookUp(
            @Query("ttbkey") String ttbkey, // 필수값 2개
            @Query("ItemId") String ItemId // 설명 : 상품을 구분짓는 유일한 값
                                           // (ItemIdType으로 정수값과 ISBN중에 택일)
    );





}
