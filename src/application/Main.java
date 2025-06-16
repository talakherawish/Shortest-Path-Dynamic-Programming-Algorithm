package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class Main extends Application {
	City[] cities;
	int cityCount;
	String startCity;
	String endCity;

	Pane root = new Pane();
	TableView<String[]> table;
	File uploadedFile;

	@Override
	public void start(Stage stage) {
		Image background = new Image(getClass().getResourceAsStream("/background.jpg"));
		ImageView backgroundView = new ImageView(background);
		backgroundView.setPreserveRatio(false);
		backgroundView.setFitWidth(1366);
		backgroundView.setFitHeight(768);

		Button load = new Button("                  \n\n\n       ");
		load.setStyle(
				"-fx-background-color: transparent; -fx-font-size: 36px; -fx-text-fill: red; -fx-font-weight: bold;");
		load.setPrefSize(300, 200);
		load.setLayoutX(650);
		load.setLayoutY(200);

		load.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open File");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
			File file = fileChooser.showOpenDialog(stage);

			if (file != null) {
				uploadedFile = file;
				readFile(file);

				if (cityCount == 0 || cities == null || cities.length == 0) {
					showAlert("No cities found in the file.");
					return;
				} else {
					programUI();
				}
			}
		});

		root.getChildren().clear();
		root.getChildren().addAll(backgroundView, load);

		Scene scene = new Scene(root, 1366, 768);
		stage.setTitle("Optimal Path DP");
		stage.setScene(scene);
		stage.show();

		scene.widthProperty().addListener((obs, oldVal, newVal) -> backgroundView.setFitWidth(newVal.doubleValue()));
		scene.heightProperty().addListener((obs, oldVal, newVal) -> backgroundView.setFitHeight(newVal.doubleValue()));
	}

	private void programUI() {
		root.getChildren().clear();
		Image background2 = new Image(getClass().getResourceAsStream("/table.png"));
		ImageView tableBack = new ImageView(background2);
		tableBack.setPreserveRatio(false);
		tableBack.setFitWidth(1366);
		tableBack.setFitHeight(768);

		table = new TableView<>();
		table.setPrefSize(1000, 400);
		table.setLayoutX(200);
		table.setLayoutY(200);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<String[], String> rowLabelCol = new TableColumn<>("City");
		rowLabelCol.setCellValueFactory(
				cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[0]));
		table.getColumns().add(rowLabelCol);

		for (int i = 0; i < cityCount; i++) {
			if (cities[i] == null)
				continue;
			final int colIndex = table.getColumns().size();
			String cityName = cities[i].name;
			TableColumn<String[], String> col = new TableColumn<>(cityName);
			col.setCellValueFactory(
					cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[colIndex]));
			table.getColumns().add(col);
		}

		for (int i = 0; i < cityCount; i++) {
			if (cities[i] == null)
				continue;
			String[] row = new String[table.getColumns().size()];
			row[0] = cities[i].name;
			Arrays.fill(row, 1, row.length, "");
			table.getItems().add(row);
		}

		Label bestPathLabel = new Label();
		bestPathLabel.setLayoutX(200);
		bestPathLabel.setLayoutY(155);
		bestPathLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #463f3a; -fx-font-weight: bold;");
		fillDpTable(table, bestPathLabel);

		Button showPaths = new Button("Show All Optimal Paths");
		showPaths.setStyle("-fx-background-color: transparent; -fx-text-fill: transparent; -fx-font-weight: bold;");
		showPaths.setOnAction(e -> showAllPaths());

		Button downloadTable = new Button("Download Table");
		downloadTable.setStyle("-fx-background-color: transparent; -fx-text-fill: transparent; -fx-font-weight: bold;");
		downloadTable.setOnAction(e -> downloadTableData());

		Button viewFile = new Button("View Uploaded File");
		viewFile.setStyle("-fx-background-color: transparent; -fx-text-fill: transparent; -fx-font-weight: bold;");
		viewFile.setOnAction(e -> viewUploadedFile());

		HBox menu = new HBox(120, downloadTable, showPaths, viewFile);
		menu.setLayoutY(660);
		menu.setLayoutX(400);

		root.getChildren().addAll(tableBack, bestPathLabel, table, menu);
	}

	private void fillDpTable(TableView<String[]> table, Label bestPathLabel) {
		int n = cityCount;
		int[][] dp = new int[n][n];
		int[][] via = new int[n][n];
		final int INF = Integer.MAX_VALUE / 2;

		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				dp[i][j] = (i == j) ? 0 : INF;

		for (int i = 0; i < n; i++) {
			City from = cities[i];
			for (int k = 0; k < from.nextCount; k++) {
				Next conn = from.next[k];
				for (int j = 0; j < n; j++) {
					if (cities[j].name.equalsIgnoreCase(conn.destination)) {
						dp[i][j] = conn.petrol + conn.hotel;
						via[i][j] = i;
						break;
					}
				}
			}
		}

		for (int mid = 0; mid < n; mid++) {
			for (int from = 0; from < n; from++) {
				for (int to = 0; to < n; to++) {
					if (dp[from][mid] + dp[mid][to] < dp[from][to]) {
						dp[from][to] = dp[from][mid] + dp[mid][to];
						via[from][to] = mid;
					}
				}
			}
		}

		int startIdx = findCityIndex(startCity);
		int endIdx = findCityIndex(endCity);

		for (int i = 0; i < n; i++) {
			String[] row = table.getItems().get(i);
			for (int j = 0; j < n; j++) {
				if (i == j) {
					row[j + 1] = "0";
				} else if (dp[i][j] < INF) {
					int mid = via[i][j];
					row[j + 1] = (mid == i) ? String.valueOf(dp[i][j]) : dp[i][j] + " (" + cities[mid].name + ")";
				} else {
					row[j + 1] = "";
				}
			}
		}

		StringBuilder bestPath = new StringBuilder();
		buildPath(startIdx, endIdx, via, bestPath);
		bestPath.append(cities[endIdx].name);
		bestPathLabel.setText("Best Path: " + bestPath + "  Cost: " + dp[startIdx][endIdx]);
	}

	private void buildPath(int from, int to, int[][] via, StringBuilder sb) {
		if (via[from][to] == from) {
			sb.append(cities[from].name).append(" → ");
		} else {
			int mid = via[from][to];
			buildPath(from, mid, via, sb);
			buildPath(mid, to, via, sb);
		}
	}

	private void downloadTableData() {
		try (PrintWriter writer = new PrintWriter("solutions.txt")) {
			// Write headers (optional for TXT, but kept for clarity)
			for (TableColumn<String[], ?> col : table.getColumns()) {
				writer.print(col.getText() + "\t");
			}
			writer.println();

			// Write ALL rows
			for (String[] row : table.getItems()) {
				for (int i = 0; i < table.getColumns().size(); i++) {
					String value = i < row.length ? row[i] : "";
					writer.print(value + (i < table.getColumns().size() - 1 ? "\t" : ""));
				}
				writer.println();
			}

			showPopup("✅ Solutions saved to solutions.txt");

		} catch (Exception e) {
			e.printStackTrace();
			showPopup("❌ Failed to save solutions: " + e.getMessage());
		}
	}

	private void showPopup(String message) {
		Stage popupStage = new Stage();
		Label label = new Label(message);
		label.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
		Scene scene = new Scene(new VBox(label), 300, 100);
		popupStage.setScene(scene);
		popupStage.setTitle("Save Status");
		popupStage.show();
	}

	private void viewUploadedFile() {
		if (uploadedFile == null) {
			showAlert("No file uploaded yet.");
			return;
		}
		Stage stage = new Stage();
		TextArea area = new TextArea();
		area.setEditable(false);
		area.setPrefSize(400, 400);
		area.setStyle("-fx-font-size: 16px;"); // Increase font size here

		try (BufferedReader br = new BufferedReader(new FileReader(uploadedFile))) {
			String line;
			while ((line = br.readLine()) != null)
				area.appendText(line + "\n");
		} catch (IOException e) {
			area.setText("Failed to read file.");
		}

		VBox box = new VBox(area);
		stage.setScene(new Scene(box, 400, 400));
		stage.setTitle("File Viewer");
		stage.show();
	}

	private void showAllPaths() {
		Stage popup = new Stage();
		TableView<String[]> altTable = new TableView<>();
		altTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<String[], String> pathCol = new TableColumn<>("Path");
		pathCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[0]));
		TableColumn<String[], String> costCol = new TableColumn<>("Cost");
		costCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[1]));
		altTable.getColumns().addAll(pathCol, costCol);

		String[][] results = new String[10000][2];
		int[] count = { 0 };
		collectPaths(findCityIndex(startCity), findCityIndex(endCity), new boolean[cityCount], 0, new StringBuilder(),
				results, count);

		Arrays.sort(results, 0, count[0], Comparator.comparingInt(r -> Integer.parseInt(r[1])));
		for (int i = 0; i < count[0]; i++) { // No Math.min, we just iterate through all
			altTable.getItems().add(new String[] { results[i][0], results[i][1] });
		}

		Label title = new Label(" Optimal Paths:");
		title.setStyle("-fx-font-size: 18px; -fx-text-fill: navy; -fx-font-weight: bold;");
		VBox layout = new VBox(10, title, altTable);
		layout.setPadding(new Insets(10));
		popup.setScene(new Scene(layout, 400, 400));
		popup.setTitle("Optimal Path Alternatives");
		popup.show();
	}

	private void collectPaths(int curr, int end, boolean[] visited, int cost, StringBuilder path, String[][] results,
			int[] count) {
		if (count[0] >= results.length)
			return;
		if (curr == end) {
			results[count[0]][0] = path.toString() + cities[end].name;
			results[count[0]][1] = String.valueOf(cost);
			count[0]++;
			return;
		}
		visited[curr] = true;
		for (int i = 0; i < cityCount; i++) {
			if (!visited[i]) {
				for (int k = 0; k < cities[curr].nextCount; k++) {
					if (cities[curr].next[k].destination.equalsIgnoreCase(cities[i].name)) {
						int nextCost = cities[curr].next[k].petrol + cities[curr].next[k].hotel;
						path.append(cities[curr].name).append(" → ");
						collectPaths(i, end, visited, cost + nextCost, path, results, count);
						path.setLength(path.length() - (cities[curr].name.length() + 3));
					}
				}
			}
		}
		visited[curr] = false;
	}

	private int findCityIndex(String name) {
		for (int i = 0; i < cityCount; i++) {
			if (cities[i].name.equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("File Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void readFile(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			int declaredCityCount = Integer.parseInt(br.readLine().trim());
			cities = new City[declaredCityCount + 2];
			int index = 0, phase = 0;
			String[] startEnd = br.readLine().trim().split(",");
			startCity = startEnd[0].trim();
			endCity = startEnd[1].trim();
			String line;
			City prev = null;
			while ((line = br.readLine()) != null && index < declaredCityCount) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				int firstComma = line.indexOf(",");
				String cityName = line.substring(0, firstComma).trim();
				City current = new City(cityName, 10, index);
				int open = line.indexOf("[");
				while (open != -1) {
					int close = line.indexOf("]", open);
					if (close == -1)
						break;
					String content = line.substring(open + 1, close);
					String[] parts = content.split(",");
					if (parts.length == 3) {
						String dest = parts[0].trim();
						int petrol = Integer.parseInt(parts[1].trim());
						int hotel = Integer.parseInt(parts[2].trim());
						current.addNext(dest, petrol, hotel);
					}
					open = line.indexOf("[", close);
				}
				if (cityName.equalsIgnoreCase(startCity))
					current.phase = 0;
				else if (prev != null && sameDestNames(current, prev))
					current.phase = prev.phase;
				else
					current.phase = ++phase;
				cities[index++] = current;
				prev = current;
			}
			boolean endExists = false;
			for (int i = 0; i < index; i++)
				if (cities[i] != null && cities[i].name.equalsIgnoreCase(endCity)) {
					endExists = true;
					break;
				}
			if (!endExists) {
				City end = new City(endCity, 0, index);
				end.phase = ++phase;
				cities[index++] = end;
			}
			br.close();
			cityCount = index;
		} catch (Exception e) {
			System.out.println("Error reading file: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean sameDestNames(City a, City b) {
		if (a.nextCount != b.nextCount)
			return false;
		for (int i = 0; i < a.nextCount; i++)
			if (!a.next[i].destination.equalsIgnoreCase(b.next[i].destination))
				return false;
		return true;
	}

	public static void main(String[] args) {
		launch();
	}
}
