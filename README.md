🍽️ FlowCamp1 ― Android 맛집/메뉴 탐색 & 관리 애플리케이션
> FlowCamp1 은 사용자가 주변 식당을 탐색하고 메뉴 – 리뷰를 기록하며, Google Maps 기반 지도와 갤러리형 메뉴 보드, 리스트·필터·정렬 기능을 제공하는 전체 오프라인-1st 맛집 관리 앱입니다.
> Android 14(API 36) / Java 11 환경에서 제작되었으며, Room-DB + LiveData + ViewModel 구조를 바탕으로 깔끔한 MVVM 지향 아키텍처로 구현되었습니다.

---

0. 목차
1. 주요 화면 흐름
2. 핵심 기능 상세
3. 데이터 모델
4. 기술 스택 & 아키텍처
5. 프로젝트 구조
6. 빌드 & 실행
7. 향후 개선 아이디어
8. 라이선스

---

1. 주요 화면 흐름
단계 | 화면 | 설명 | 파일(주요 클래스)
---- | ---- | ---- | -----------------
① | Splash | 첫 실행 시 스플래시 애니메이션; 위치·저장공간 등 필수 권한 요청 | SplashActivity
② | MainActivity | BottomNavigationView 로 3 개의 탭(Home·Gallery·Map) 전환 | MainActivity
③ | Home 탭 | 식당 리스트(RecyclerView); 필터(CuisineType Spinner)·정렬(Dialog)·검색; 빈 화면 안내 레이아웃 | HomeFragment, HomeAdapter
④ | Gallery 탭 | 메뉴 보드(GridLayout); 메뉴 카드 클릭 → 상세 BottomSheet | GalleryFragment, MenuDetailBottomSheet
⑤ | Map 탭 | Google Maps & Places SDK; 식당 마커 표시 및 하이라이트; 현재 위치·Zoom·Marker 클릭 시 하단 카드 | MapFragment
⑥ | Add Restaurant/Menu | 커스텀 다이얼로그(DialogFragment) 2종; Place Picker Activity 호출 → 위·경도 자동 입력; 갤러리/카메라 이미지 선택, Glide 미리보기 | AddRestaurantDialogFragment, AddMenuDialogFragment, PlacePickerActivity

---

2. 핵심 기능 상세

2-1. 스플래시 & 권한 관리
기능 | 상세
--- | ---
SplashScreen API | Android 12 이상 SplashScreen Compat 사용, 브랜딩 이미지 페이드-인
런타임 권한 | ActivityCompat.requestPermissions → 결과 콜백에서 미승인 시 Alert Dialog 로 가이드
Overlay 권한 체크 | 다중 창(오버레이) 기능을 위한 ACTION_MANAGE_OVERLAY_PERMISSION 인텐트 처리

2-2. Home Fragment – 레스토랑 탐색
- LiveData 관찰로 실시간 리스트 반영
- CuisineType 필터: ALL / KOREAN / WESTERN / CHINESE / JAPANESE / OTHER
- 정렬 옵션:
  인덱스 | Enum | 의미
  ------ | ---- | ----
  0 | DATE_DESC | 최근 등록순 ↓
  1 | DATE_ASC | 과거 등록순 ↑
  2 | DISTANCE_ASC | 현 위치 ↔ 거리순 ↑
  3 | NAME_ASC / DESC | 이름 가나다·역순
  5 | RATING_ASC / DESC | 평점 낮→높·높→낮
- Empty State: DB 비어있을 때 empty_restaurant.xml 표시
- 애니메이션: 리스트 확장/축소 시 AnimationUtils.expand()/collapse()로 부드러운 ValueAnimator 처리

2-3. Gallery Fragment – 메뉴 갤러리
- GridLayoutManager(2-열), 카드 스타일 item_menuboard.xml
- Glide v4 로 Lazy-Loading 이미지
- BottomSheet (MenuDetailBottomSheet):
  메뉴 사진·이름·가격·평점(★)·리뷰·소속 식당명을 한눈에
  즐겨찾기/공유 버튼 예비 슬롯 포함
- Sort/Filter 재사용: Home 탭 로직 재활용 (SRP 준수)

2-4. Map Fragment – Google Maps 통합
요소 | 내용
---- | ----
지도 초기화 | MapsInitializer.initialize() + 커스텀 map_style.json 적용(POI 라벨 최소화)
마커 관리 | HashMap<Long, Marker> 로 Restaurant ID ↔ Marker 매핑, 라이프사이클에 안전한 클린-업
현재 위치 | FusedLocationProviderClient → 지도 My Location 버튼 및 거리 계산
Restaurant 선택 | 마커 클릭 → ViewModel 에 선택 값 저장 → 하단 카드 뷰 노출 & Marker 색상 변화
PlacePicker 연동 | 지도 화면에서 “추가” 누르면 별도 PlacePickerActivity 실행 (Autocomplete + 드래그 핀)

2-5. 데이터 입력 다이얼로그
- AddRestaurantDialogFragment:
  1. 식당명·카테고리 입력 → 2. 주소 검색(PlacePicker) → 3. 저장
- AddMenuDialogFragment:
  1. 메뉴명·가격·평점·리뷰 입력 → 2. 갤러리/카메라 이미지 선택 → 3. 저장
- 모든 Insert 작업은 DBRepository.io(Executor) 비동기 스레드에서 수행 후 Main-Thread Handler로 콜백

---

3. 데이터 모델
Entity | 주요 필드 | 설명
------ | -------- | ----
Restaurant | id, name, location, detailedLocation, cuisineType, latitude, longitude | 식당 기본 정보
MenuItem | id, restaurant_id(fk), menuName, imageUri, price, rating, review | 메뉴 단위 리뷰
CuisineType (Enum) | displayName(한글) | 카테고리 필터용
SortOrder (Enum) | - | 리스트 정렬 내부 로직

Room
@Database(entities = {Restaurant.class, MenuItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase { ... }

- TypeConverter Converters – Uri ↔ String, CuisineType ↔ String

---

4. 기술 스택 & 아키텍처
Layer | 라이브러리/기술 | 비고
----- | -------------- | ----
UI | Material 3, ViewBinding, Glide, BottomSheetDialog |
Presentation | ViewModel, LiveData, Navigation Component | 단일 Activity - 멀티 Fragment 패턴
Domain/Repository | 자체 DBRepository | 비동기 I/O + MainThread 콜백
Data | Room Database, Gson(시드 JSON 파싱) | 내부 SQLite
Map/Location | Google Maps SDK v3.5, Places API, Fused Location |
Etc. | SplashScreen API, ValueAnimator |

아키텍처 패턴: MVVM (+ Repository)
의존성 주입은 사용하지 않았으며, 규모 확대 시 Hilt 도입이 용이하도록 인터페이스 기반 설계.

---

5. 프로젝트 구조
flowcamp1/
 ├─ app/
 │  ├─ src/main/
 │  │  ├─ java/com/example/myapp/
 │  │  │  ├─ ui/
 │  │  │  │  ├─ home/         # HomeFragment·어댑터
 │  │  │  │  ├─ gallery/      # GalleryFragment·BottomSheet·AddDialogs
 │  │  │  │  └─ map/          # MapFragment·PlacePicker
 │  │  │  ├─ data/
 │  │  │  │  ├─ dao/          # Room DAO
 │  │  │  │  ├─ db/           # AppDatabase & Converters
 │  │  │  │  └─ *.java        # Entity·Enum
 │  │  │  ├─ animations/      # View expand/collapse util
 │  │  │  └─ MainActivity.java
 │  │  ├─ res/
 │  │  │  ├─ layout/          # XML 레이아웃 15 종
 │  │  │  ├─ navigation/      # mobile_navigation.xml
 │  │  │  ├─ raw/map_style.json
 │  │  │  └─ values*          # 기본·다국어 스트링
 ├─ build.gradle.kts
 └─ settings.gradle.kts

---

6. 빌드 & 실행
1. 필수 도구
   - Android Studio <2025> Hedgehog 이상
   - JDK 11
2. Google API 키 등록
   1. secrets.properties 파일 생성 또는 편집
   2. 다음 두 항목을 입력
      GOOGLE_MAPS_API_KEY=AIzaSy...
      GOOGLE_PLACES_API_KEY=AIzaSy...
3. 의존성 다운로드 & 빌드
   ./gradlew clean assembleDebug
4. 실행
   - USB/Emulator → Run ‘app’
   - 최초 실행 시 위치·저장공간 권한 허용 필요

---

7. 향후 개선 아이디어
카테고리 | 제안
-------- | ----
UX | 다크 모드 대응; 마커 클러스터링 & Heatmap 시각화
데이터 | Cloud Sync(Firebase Cloud Store); AI 리뷰 요약 & 추천 메뉴
구조 | DI(Hilt) 도입, Clean Architecture 계층화
테스트 | JUnit + Espresso UI 테스트 보강
CI/CD | GitHub Actions → Firebase App Distribution 자동 배포

---

8. 라이선스
본 저장소는 MIT License를 따릅니다. 자세한 내용은 LICENSE 파일을 확인하세요.
