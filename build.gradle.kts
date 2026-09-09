dependencies {
	// Работа с БД
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Веб-сервер (Правильное название: web, а не webmvc)
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Драйвер PostgreSQL
	runtimeOnly("org.postgresql:postgresql")

	// Библиотеки для тестов (Правильное название: test, а не webmvc-test)
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}