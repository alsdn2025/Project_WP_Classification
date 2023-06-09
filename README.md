#### Project_WP_Classification
#### Repo for sharing with 'general design' team members(TJ, JC, YG)
<br><br>


# 제주스타그램
![image](https://github.com/alsdn2025/Project_WP_Classification/assets/77447518/13341368-fafe-4229-b631-1278605e8f8b)
#### 제주스타그램은 제주도에 서식하는 자생식물들의 도감을 만들어가는 애플리케이션입니다. 
#### 사용자는 제주도의 자생식물들을 직접 촬영하고 정보를 알아내며, 마치 포켓몬 도감처럼 이를 수집할 수 있습니다. 
<br><br>


# 이미지 분류 모델
![image](https://github.com/alsdn2025/Project_WP_Classification/assets/77447518/42ff88fe-bd14-4c49-a742-f177e86bbbdb)
![image](https://github.com/alsdn2025/Project_WP_Classification/assets/77447518/73554e86-f280-4343-80e8-0852dfbc6f18)
#### 해당 애플리케이션은 Google TensorFlow Lite 라이브러리를 활용하여 만들어진 이미지 분류 모델을 통하여, 
#### 총 30가지의 자생식물의 잎, 열매, 꽃을 인식하고 사용자에게 해당 식물에 대한 정보를 제공합니다.
<br><br>


## 기능(1) : 식물 분류
![image](https://github.com/alsdn2025/Project_WP_Classification/assets/77447518/8c59922f-f00c-4f0b-8d2f-f05ae4cdd50a)
#### 사용자가 식물의 특정 부위(잎, 열매, 꽃)의 사진을 촬영하거나 선택하여 제출하면, 
#### 앱에 내장된 이미지 분류 모델이 이를 기반으로 식물의 이름을 분류해냅니다. 
#### 분류가 끝나면 사용자는 해당 식물의 이름, 추론 확률을 제공받으며, 
#### 추가적으로 독성이나 식용 여부 등을 포함한 상제 정보를 열람할 수 있습니다. 
#### 이를 통해 사용자는 제주도에서 발견한 식물들을 쉽게 식별하고 관련 정보를 얻을 수 있습니다.
<br><br>


## 기능(2) : 게시글 포스팅
![image](https://github.com/alsdn2025/Project_WP_Classification/assets/77447518/70e7f62c-5ec9-4884-8777-909c7062dfd3)
#### 사용자는 찾아낸 식물을 애플리케이션의 게시판에 포스팅할 수 있습니다. 
#### 해당 식물을 발견한 위치, 날짜, 코멘트 등을 포함하여 게시글을 작성하고, 순간을 추억할 수 있습니다. 
#### 식물에 관한 경험을 저장하고 교류할 수 있는 공간입니다.
<br><br>

## 기능(3) : 식물 도감 (컬렉션)
![image](https://github.com/alsdn2025/Project_WP_Classification/assets/77447518/de37ddf6-077c-439d-9469-9bff566b967d)
#### 제주도에 서식하는 자생식물의 컬렉션을 모두 모아보세요. 
#### 애플리케이션 내에서 제공되는 컬렉션 화면에서 나의 컬렉션 완성도를 확인하고, 
#### 각 식물의 세부 정보와 함께 사용자가 찍은 사진을 확인할 수 있습니다. 
<br>
![image](https://github.com/alsdn2025/Project_WP_Classification/assets/77447518/ddde7d3b-50fb-4abe-beb7-db183aee560b)
#### 모든 컬렉션을 완성할 시, 특별한 이펙트가 재생됩니다. 
#### 제주도의 다양한 식물들을 찾아가며 컬렉션을 완성해보세요!
<br><br>


## 추가 기능 3: SNS 공유 (계획중)
#### (아직 개발 중인 추가 기능입니다. )
#### 애플리케이션을 통해 수집한 식물의 사진, 위치, 설명 등을 카카오톡, 페이스북 등을 통해 손쉽게 공유할 수 있습니다. 
#### 다른 사람들과 식물에 대한 관심과 경험을 공유하고 함께 자연을 사랑하는 커뮤니티를 형성해보세요.
<br><br>


### License
#### 이 애플리케이션은 MIT License를 따릅니다.
<br><br>



### 데이터셋
#### 해당 애플리케이션의 분류 모델 학습시 사용된 데이터셋은 AI HUB 에서 제공받았습니다. 
#### (https://www.aihub.or.kr/) 
<br><br>


### 외부 라이브러리
#### 프로그래스 바 : https://github.com/akexorcist/RoundCornerProgressBar
#### 완성 이펙트 : https://github.com/DanielMartinus/konfetti
<br><br>
