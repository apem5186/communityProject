# communityProject

기본적인 웹 기술을 이용한 게시글 프로젝트

### 개발 인원
1명

### 프로젝트 기간

2023-07-18 ~ 2023-09-18

### 프로젝트 기능

#### - 사용자
  - 회원가입 및 로그인
  - OAuth2를 이용한 카카오 로그인
  - jwt를 통한 보안
  - 프로필 및 수정, 삭제
  - s3를 통한 프로필 사진 업로드
  - 프로필 내에서 활동 내역 확인

#### - 게시글
  - 카테고리별 게시판 기능
  - 좋아요, 즐겨찾기, 조회수 기능
  - 카테고리별 검색 및 페이징
  - s3를 통한 사진 업로드

#### - 댓글
  - 댓글과 대댓글 기능
  - 각 댓글에 좋아요 기능
  - 무한스크롤

#### - 관리자
  - 관리자 페이지를 이용한 회원, 게시글, 댓글 관리
  - 관리자 페이지 내에서 페이징 및 세부적인 검색 기능
  - 공지 게시글 업로드 권한
  - 카카오 로그인을 제외한 일반 사용자와 같은 기능

### 사용 기술 및 프레임워크

- Java 17
- SpringBoot 3.1.1
- Spring Data JPA
- Spring Security
- OAuth2
- Jwt
- Spring Cloud AWS

#### 웹 개발 및 템플릿

- Thymeleaf
- Html/CSS
- BootStrap 5.3.1
- JavaScript

#### 데이터베이스

- MySql 8.0.26
- Redis 3.0.5
- H2

#### 테스트도구
- JUnit
- Postman

#### Build Tool
- Gradle 7.3.3



<details>
  <summary><h3>DB 설계</h3></summary>
  <image src="https://github.com/apem5186/communityProject/assets/81023500/0aa2a9b3-c7d1-44d5-85b3-22a811743260"/>
</details>



### 주요 실행 화면
  
<details>
  <summary><h4>사용자</h4></summary>

> **1. 회원가입**
> 
> <image src="https://github.com/apem5186/communityProject/assets/81023500/74cf8c81-f3cb-4b9a-8140-8fa05c7c1bac"/>
>
> **2. 로그인**
> 
> <image src="https://github.com/apem5186/communityProject/assets/81023500/ba30ac6b-1f9a-4172-8399-4a0bbd055aac"/>
>
>  **3. 프로필**
> 
> <image src="https://github.com/apem5186/communityProject/assets/81023500/22c69704-9d44-4bbc-9bcd-a8bc3beb2672"/>





</details>

<details>
  <summary><h4>게시글</h4></summary>

> **1. 메인페이지**
>
> <image src="https://github.com/apem5186/communityProject/assets/81023500/55035957-1fbe-49ab-9696-1ab0d15fc523"/>
>
>
> **2. 게시판 카테고리**
> 
> <image src="https://github.com/apem5186/communityProject/assets/81023500/91ceb1e3-692f-4231-90a8-e2197ee36d5c"/>
>
> **3. 게시글**
>
> <image src="https://github.com/apem5186/communityProject/assets/81023500/6fbdeffb-ce44-40ee-9ed3-0e290912630f"/>
>
> **4. 수정 및 삭제**
>
> <image src="https://github.com/apem5186/communityProject/assets/81023500/edc8aa60-3776-474e-89e0-b2dae31f39ce"/>

      
</details>

<details>
  <summary><h4>댓글</h4></summary>

> **1. 댓글**
>
> <image src="https://github.com/apem5186/communityProject/assets/81023500/b939fabc-0b3c-4060-a020-1f199cfb16a3"/>

</details>

<details>
  <summary><h4>관리자</h4></summary>

> **1. 게시글 관리**
>
> <image src="https://github.com/apem5186/communityProject/assets/81023500/61b78e7b-4463-490b-9f64-073a8f4a0b46"/>
>
> **2. 댓글 관리**
>
> <image src="https://github.com/apem5186/communityProject/assets/81023500/a4711984-7871-4564-9ab6-dbf9a6e0b6b9"/>
>
> **3. 유저 관리**
>
> <image src="https://github.com/apem5186/communityProject/assets/81023500/9b023403-c5f8-4e50-9e8f-91e444e84e92"/>



</details>
