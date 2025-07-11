# 🍽️ 맛카 (Matka) ― 나만의 맛집 아카이빙 도우미

> 내가 방문한 맛집, 먹었던 메뉴들을 손쉽게 기록하고 관리할 수 있는 개인 맞춤형 맛집 플랫폼입니다.

<br>

## 🛠️ 개발 환경 (Environment)
- **OS:** Android 14 (API 36)
- **Language:** Java 11

<br>

## 📝 목차 (Table of Contents)
1. [팀원 소개](#-팀원-소개-team)
2. [주요 화면 흐름](#-주요-화면-흐름-main-flow)
3. [핵심 기능 상세](#-핵심-기능-상세-core-features)
4. [APK 다운로드](#-apk-다운로드)

<br>

## 🧑‍🤝‍🧑 팀원 소개 (Team)

- **박세린** - 고려대학교 23학번 (04년생)
- **석인호** - KAIST 23학번 (05년생)

<br>

## 🏞️ 주요 화면 흐름 (Main Flow)

### 1. 스플래시 (Splash Screen)
- 앱 구동 시 필요한 데이터 로딩, API 설정, 권한 요청을 수행합니다.
- 모든 준비가 완료되면 메인 화면으로 전환됩니다.

### 2. 메인 화면 (Main Screen)
메인 화면은 하단 네비게이션 바를 통해 **맛집 탭**, **메뉴 탭**, **지도 탭** 세 가지 주요 기능으로 접근할 수 있습니다.

- **맛집 탭 (Restaurants Tab)**
    - 저장한 맛집들이 리스트 형태로 표시됩니다.
    - 각 항목에는 식당 이름, 종류, 평균 별점, 거리, 주소가 기본 정보로 나타납니다.
    - 리스트를 터치하면 펼쳐지며 '메뉴판 보기' 버튼과 기록된 메뉴 사진들을 확인할 수 있습니다.

- **메뉴 탭 (Menu Gallery Tab)**
    - 등록한 모든 메뉴 사진들이 갤러리 형태로 나타납니다.
    - 사진을 클릭하면 메뉴명, 가격, 한줄평, 가게 정보(이름, 주소)를 볼 수 있습니다.

- **지도 탭 (Map Tab)**
    - 저장한 맛집들이 지도 위에 마커로 표시되어 위치를 한눈에 파악하기 용이합니다.
    - 마커를 클릭하면 해당 맛집의 정보와 메뉴 정보가 카드 형태로 나타납니다.

### 3. 맛집 등록 (Add Restaurant)
- **맛집 탭** 또는 **지도 탭**의 `+` 버튼을 통해 맛집을 등록할 수 있습니다.
- **수동 입력:** 이름과 주소를 직접 입력하여 등록합니다.
- **지도에서 검색:**
    - '지도에서 위치 검색' 버튼을 누르면 내 위치 중심의 지도가 나타납니다.
    - 지도에서 위치를 직접 선택하거나 장소를 검색하면, 해당 위치의 정보 카드가 표시됩니다.
    - '이 위치로 맛집 등록하기' 버튼을 누르면 주소와 이름이 자동으로 입력됩니다.
- 맛집 등록 즉시 **맛집 탭**과 **지도 탭**에 반영됩니다.

### 4. 메뉴 추가 (Add Menu)
- **메뉴 탭**의 `+` 버튼을 통해 메뉴를 추가합니다.
- 카메라로 사진을 직접 찍거나 갤러리에서 사진을 선택하면 메뉴 추가 다이얼로그가 나타납니다.
- 다이얼로그에서 메뉴명, 맛집명, 한줄평(선택), 가격, 별점을 입력하여 메뉴를 추가합니다.
- 추가된 메뉴는 즉시 **메뉴 탭**과 해당 맛집의 상세 정보에서 확인할 수 있습니다.

### 5. 수정 및 삭제 (Edit / Delete)
- 앱 내 정보에 접근할 수 있는 모든 곳(3개의 탭, 상세 정보, 카드뷰 등)에서 수정 및 삭제가 가능합니다.
- 메뉴 정보 수정 시, 연결된 맛집명은 변경할 수 없습니다.
- 메뉴판 이미지는 **맛집 탭 → 메뉴판 보기 → 메뉴판 수정**을 통해 변경할 수 있습니다.

<br>

## ✨ 핵심 기능 상세 (Core Features)

### 1. 스플래시 & 권한 관리
- `SplashScreen API`를 사용하여 Cold Start 시 초기 화면을 구성했습니다.
- 앱 실행 시 500ms의 애니메이션과 함께 Google Map API 및 Places API를 초기화합니다.
- 위치 정보 권한이 없는 경우, 이 단계에서 사용자에게 권한을 요청합니다.

### 2. 맛집 탭 (Home Fragment)
- **실시간 데이터 업데이트**: 맛집 정보를 `LiveData`로 관찰하여 데이터 변경이 즉시 UI에 반영되도록 구현했습니다.
- **데이터베이스 (Restaurant DB)**
    - `id` (PK, Auto-generate), `name`, `menuBoardUri`, `location`, `detailedLocation`, `cuisineType`, `latitude`, `longitude`
- **음식 종류 (Cuisine Type)**: `한식`, `양식`, `중식`, `일식`, `기타` 5가지 유효한 카테고리로 분류됩니다. (DB상에는 7종 존재)
- **정렬 및 필터링**
    - **정렬 옵션**: 등록순, 거리순, 이름순, 평점순 정렬을 지원합니다.
    - **필터링**: 전체 및 5가지 음식 종류별로 맛집을 필터링해서 볼 수 있습니다.
- **UI/UX 개선**
    - 등록된 맛집이 없을 경우, 안내 캐릭터가 포함된 빈 화면을 표시합니다.
    - 리스트 확장/축소 시 자연스러운 `expand/collapse` 애니메이션을 적용했습니다.
    - 상세 정보의 메뉴를 클릭하면 BottomSheet 형태로 메뉴 상세 정보가 나타납니다.
    - 현재 위치를 가져오지 못하면 거리가 표시되지 않으며, 메뉴판이 없으면 '메뉴판 보기' 버튼이 숨겨집니다.
    - **탭 이동 시 상태 유지**: 정렬 및 필터링, 리스트 열림 상태는 다른 탭으로 이동했다가 돌아와도 그대로 유지됩니다.

### 3. 메뉴 탭 (Gallery Fragment)
- **실시간 데이터 업데이트**: 메뉴 정보 역시 `LiveData`를 통해 실시간으로 UI에 반영됩니다.
- **데이터베이스 (MenuItem DB)**
    - `id` (PK, Auto-generate), `menuName`, `imageUri`, `rating`, `review`, `restaurantId` (FK), `price`
    - `On Delete Cascade` 옵션을 적용하여, 연결된 맛집(Restaurant)이 삭제되면 해당 맛집의 메뉴들도 함께 삭제됩니다.
- **정렬 및 필터링**
    - **정렬 옵션**: 등록순, 이름순, 평점순, 가격순 정렬을 지원합니다.
    - **필터링**: 맛집 탭과 동일하게 음식 종류별 필터링이 가능합니다.
- **UI/UX 개선**
    - 등록된 메뉴가 없을 경우, 안내 캐릭터가 포함된 빈 화면을 표시합니다.
    - 메뉴 클릭 시 상세 정보가 담긴 BottomSheet가 나타납니다.
    - **보기 모드 변경**: 그리드 아이콘을 통해 한 줄에 2개 또는 3개의 메뉴를 표시하는 모드를 토글할 수 있습니다. (2개 모드에서는 메뉴명과 맛집명이 추가로 보임)
    - **탭 이동 시 상태 유지**: 정렬 및 필터링 상태는 다른 탭으로 이동했다가 돌아와도 그대로 유지됩니다.

### 4. 지도 탭 (Map Fragment)
- **Custom Map**: 기본 Google Map에서 비즈니스 POI를 제외한 라벨을 비활성화하여 깔끔한 지도를 제공합니다.
- **마커 및 지도 시점**
    - 저장된 맛집은 연주황색 마커로 표시됩니다.
    - 지도 초기 시점은 모든 마커가 가장 잘 보이도록 자동으로 설정됩니다.
    - 마커 클릭 시, 해당 맛집 위치로 지도가 확대(줌 레벨 17)되며 상세 정보 카드가 나타납니다.
    - 필터링 시, 필터링된 마커들이 잘 보이도록 지도 시점이 재설정됩니다.
- **POI 연동 맛집 등록**: 지도 위의 POI 라벨을 클릭하면 장소 정보를 확인하고, 버튼 하나로 간편하게 맛집을 등록할 수 있습니다.

### 5. 데이터 입력 다이얼로그 (Dialogs)
- **입력 유효성 검사**: 필수 필드를 채우지 않거나 최대 글자 수를 초과하면 에러 메시지가 표시됩니다.
- **버튼 활성화/비활성화**: 모든 입력 조건이 만족될 때만 '저장' 버튼이 활성화(노란색)되어 사용자 실수를 방지합니다.
- **'추가' 및 '수정' 모드**:
    - **`AddRestaurantDialogFragment`**: 맛집 추가 및 수정
    - **`AddMenuDialogFragment`**: 메뉴 추가 및 수정
    - 수정 모드에서는 기존에 등록된 정보가 채워진 상태로 다이얼로그가 열립니다.
- **비동기 처리**: 모든 데이터베이스 Insert 작업은 `Executor`를 사용한 비동기 스레드에서 처리 후, `Handler`를 통해 Main 스레드로 결과를 콜백하여 UI를 안전하게 업데이트합니다.

<br>

## 시연 영상
> [영상 보기](https://youtube.com/shorts/oJj2K_SgdtU?feature=share)

## 📲 APK 다운로드
> [다운로드](https://drive.google.com/file/d/1hqpBxtxvWlzr6yBqzPzfLHLeG9gbO47X/view?usp=sharing)
