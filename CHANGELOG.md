# CHANGELOG
## 0.2.49 (2017.04.29)
- onInit과 onStateChange.INIT이벤트가 동일하게 Remon객체가 온전히 생성되었고 Remon서버와 연결되었을 때 호출되게 변경
- 방송 기능 일부 추가(베타기능)
- 항상 하나의 Remon객체만 호출되고 종료될 수 있는 singleton factory를 제공. RemonSingleFactory

## 0.2.4 (2017.01.09)
speaker phone mode false안되는 것 수정

## 0.0.11 (2016.12.07)
추가: 최초 배포

## 0.0.13 (2016.12.15)
수정: 상대 peer에서 close시 mediamanager를 close하지 않은것을 close하도록 수정
수정: audiomode를 peer간 연결 후 communication mode 변경되도록 수정

## 0.0.14 (2016.12.22)
수정: 기본 코덱을 h.264로 변경

## 0.0.15 (2016.12.23)
수정: Observer가 없으면 기본 observer 생성

## 0.1.0 (2016.12.23)
__: 0.0.15버전과 변화 없으며 버전관리를 위해 버전 업데이트
