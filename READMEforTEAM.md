## MW 2022/12/03

### DBHelper 클래스에 메서드 추가 
### DB 구조는 건들지 않음

### 컬렉션 기능을 위한 CollectionFragment.java , CollectionManager.java, CollectionImgAdapter.java , GridItemDTO.java 추가
### UserFragment 안쓰임
### Collection 기능 공지 
####: 최종 시연 영상 촬영용으로, 수집 못한 아이템일 지라도 클릭시 수집되도록(치트) 수정했습니다 
#### 이 때 Collection Fragment 를 벗어났다가 다시 오면 이미지가 가려져 있습니다. 
#### 이는 정상이며, 애초에 일회성 촬영용 치트 기능이므로 무시해도 됩니다. 


## MainActivity.kt 컬렉션 기능을 위한 CollectionManager 등 내용 추가, 기존 User 버튼 이벤트 삭제, CollectionFragment 호출로 변경 

### TO TJ > 컬렉션 기능 dictionary.json 파일을 이용해서 만듦, read 만 했음, 수정 x

## build.gradle(Module:My_App1.app)  수정  
#### progress bar , party effect 의존성 추가 
#### 외부 의존성 다수 사용( glide, 파티 이펙트, 프로그래스 바 등 )
## manifest 는 변경 x ( 마지막 수정일 2022-11-27 )
## res>layout, res>drawable, res>color 다수 추가 & 수정 

## 염곤씨 카메라 수정사항 ( 11/28 ) 반영 안함, 이번 주 진척사항 마무리 후 반영 예정 


