package com.school.weatherapp.ui.panels;

import com.school.weatherapp.features.FavoriteCity;
import com.school.weatherapp.features.FavoritesService;
import com.school.weatherapp.features.UserPreferencesService;
import com.school.weatherapp.util.ThemeUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * FavoritesPanel - UI panel for displaying and managing favorite cities.
 *
 * Enhancements in this version:
 * - User can choose how favorite groups are built:
 *   (1) Country groups (existing behavior)
 *   (2) Custom groups (user-defined groups containing multiple country codes)
 * - User can choose how groups are ordered:
 *   (1) Alphabetical
 *   (2) Manual order (saved to preferences)
 */
public class FavoritesPanel extends VBox {

    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private final FavoritesService favoritesService;
    private final UserPreferencesService preferencesService;

    private Consumer<String> onCitySelectCallback;
    private Runnable onFavoritesChangeCallback;

    private VBox favoritesList;
    private Label headerLabel;
    private ScrollPane scrollPane;

    // Controls
    private ComboBox<String> groupingCombo;
    private ComboBox<String> orderCombo;
    private Button manageGroupsButton;
    private Button reorderGroupsButton;

    public FavoritesPanel(FavoritesService favoritesService, UserPreferencesService preferencesService) {
        this.favoritesService = favoritesService;
        this.preferencesService = preferencesService;

        setPadding(new Insets(12));
        setSpacing(10);
        setMinWidth(300);
        setPrefWidth(420);
        setMaxWidth(520);
        setMinHeight(0);
        setPrefHeight(USE_COMPUTED_SIZE);
        setMaxHeight(Double.MAX_VALUE);

        // Panel styling via CSS.
        getStyleClass().add("panel-background");

        buildHeader();
        buildControls();
        buildFavoritesList();
        refreshFavorites();

        // Default theme application (MainApp also controls stylesheet ordering).
        applyLightTheme();
    }

    // -------------------- Theme Methods --------------------

    public void applyLightTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_LIGHT, THEME_DARK);
    }

    public void applyDarkTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_DARK, THEME_LIGHT);
    }

    // -------------------- Public API --------------------

    public void setOnCitySelectCallback(Consumer<String> callback) {
        this.onCitySelectCallback = callback;
    }

    public void setOnFavoritesChangeCallback(Runnable callback) {
        this.onFavoritesChangeCallback = callback;
    }

    public void refreshFavorites() {
        favoritesList.getChildren().clear();

        List<FavoriteCity> favorites = favoritesService.getFavoriteEntries();
        if (favorites.isEmpty()) {
            showEmptyState();
            return;
        }

        showFavorites(favorites);
    }

    // -------------------- UI Build --------------------

    private void buildHeader() {
        headerLabel = new Label("Favorites");
        headerLabel.getStyleClass().add("panel-title");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setTextAlignment(TextAlignment.LEFT);

        getChildren().add(headerLabel);
    }

    private void buildControls() {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        groupingCombo = new ComboBox<>();
        groupingCombo.getItems().addAll("Group: Country", "Group: Custom");
        groupingCombo.setPrefWidth(160);

        orderCombo = new ComboBox<>();
        orderCombo.getItems().addAll("Order: A–Z", "Order: Manual");
        orderCombo.setPrefWidth(140);

        manageGroupsButton = new Button("Manage Groups");
        reorderGroupsButton = new Button("Reorder");

        // Load current prefs
        applyPrefsToControls();

        groupingCombo.setOnAction(e -> {
            preferencesService.setFavoritesGroupingMode(parseGroupingMode(groupingCombo.getValue()));
            applyPrefsToControls();
            refreshFavorites();
        });

        orderCombo.setOnAction(e -> {
            preferencesService.setFavoritesGroupOrderMode(parseOrderMode(orderCombo.getValue()));
            refreshFavorites();
        });

        manageGroupsButton.setOnAction(e -> {
            openManageCustomGroupsDialog();
            refreshFavorites();
        });

        reorderGroupsButton.setOnAction(e -> {
            openReorderGroupsDialog();
            refreshFavorites();
        });

        row.getChildren().addAll(groupingCombo, orderCombo, manageGroupsButton, reorderGroupsButton);

        Separator sep = new Separator();
        sep.setPadding(new Insets(2, 0, 2, 0));

        getChildren().addAll(row, sep);
    }

    private void applyPrefsToControls() {
        UserPreferencesService.FavoritesGroupingMode groupingMode = preferencesService.getFavoritesGroupingMode();
        UserPreferencesService.FavoritesGroupOrderMode orderMode = preferencesService.getFavoritesGroupOrderMode();

        groupingCombo.setValue(groupingMode == UserPreferencesService.FavoritesGroupingMode.CUSTOM
                ? "Group: Custom"
                : "Group: Country");

        orderCombo.setValue(orderMode == UserPreferencesService.FavoritesGroupOrderMode.MANUAL
                ? "Order: Manual"
                : "Order: A–Z");

        boolean isCustom = groupingMode == UserPreferencesService.FavoritesGroupingMode.CUSTOM;
        manageGroupsButton.setDisable(!isCustom);
    }

    private void buildFavoritesList() {
        favoritesList = new VBox(10);
        favoritesList.setFillWidth(true);

        scrollPane = new ScrollPane(favoritesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(scrollPane);
    }

    // -------------------- Rendering --------------------

    private void showEmptyState() {
        Label emptyState = new Label("No favorites yet. Search a city and add it to favorites.");
        emptyState.getStyleClass().add("label-secondary");
        emptyState.setWrapText(true);
        emptyState.setPadding(new Insets(12, 6, 12, 6));
        favoritesList.getChildren().add(emptyState);
    }

    private void showFavorites(List<FavoriteCity> favorites) {
        Accordion accordion = new Accordion();
        accordion.getStyleClass().add("favorites-group-container");

        UserPreferencesService.FavoritesGroupingMode groupingMode = preferencesService.getFavoritesGroupingMode();
        UserPreferencesService.FavoritesGroupOrderMode orderMode = preferencesService.getFavoritesGroupOrderMode();

        Map<String, List<FavoriteCity>> grouped = groupingMode == UserPreferencesService.FavoritesGroupingMode.CUSTOM
                ? groupByCustomGroups(favorites)
                : groupByCountry(favorites, orderMode);

        List<String> groupKeys = new ArrayList<>(grouped.keySet());
        groupKeys = orderGroups(groupKeys, groupingMode, orderMode);

        for (String key : groupKeys) {
            List<FavoriteCity> cities = grouped.getOrDefault(key, new ArrayList<>());
            if (cities.isEmpty()) {
                continue;
            }
            cities.sort(buildCityComparator());

            VBox groupList = new VBox(6);
            groupList.getStyleClass().add("favorites-group-list");
            for (FavoriteCity city : cities) {
                groupList.getChildren().add(createCityItem(city));
            }

            TitledPane groupPane = new TitledPane(key, groupList);
            groupPane.getStyleClass().add("favorites-group-pane");
            groupPane.setExpanded(true);

            accordion.getPanes().add(groupPane);
        }

        favoritesList.getChildren().add(accordion);
    }

    private Map<String, List<FavoriteCity>> groupByCountry(List<FavoriteCity> favorites,
                                                          UserPreferencesService.FavoritesGroupOrderMode orderMode) {

        // Keep the existing country-code-first grouping behavior.
        Map<String, List<FavoriteCity>> groupedFavorites;

        if (orderMode == UserPreferencesService.FavoritesGroupOrderMode.ALPHABETICAL) {
            groupedFavorites = new TreeMap<>(buildCountryCodeComparator());
        } else {
            // For manual order, keep insertion order then apply ordering later.
            groupedFavorites = new LinkedHashMap<>();
        }

        for (FavoriteCity city : favorites) {
            String countryCode = normalizeCountryCode(city.getCountryCode());
            groupedFavorites.computeIfAbsent(countryCode, key -> new ArrayList<>()).add(city);
        }

        return groupedFavorites;
    }

    private Map<String, List<FavoriteCity>> groupByCustomGroups(List<FavoriteCity> favorites) {
        List<UserPreferencesService.CustomCountryGroup> groups = preferencesService.getCustomCountryGroups();


        Map<String, List<FavoriteCity>> result = new LinkedHashMap<>();

        // Create each custom group bucket
        for (UserPreferencesService.CustomCountryGroup g : groups) {
            result.put(g.getName(), new ArrayList<>());
        }

        // Distribute cities into matching custom groups.
        for (FavoriteCity city : favorites) {
            String cc = normalizeCountryCode(city.getCountryCode());
            boolean placed = false;

            for (UserPreferencesService.CustomCountryGroup g : groups) {
                Set<String> groupCodes = g.getCountryCodes()
                        .stream()
                        .map(this::normalizeCountryCode)
                        .collect(Collectors.toSet());

                if (groupCodes.contains(cc)) {
                    result.get(g.getName()).add(city);
                    placed = true;
                }
            }

            if (!placed) {
                result.computeIfAbsent("Other", k -> new ArrayList<>()).add(city);
            }
        }

        return result;
    }

    private List<String> orderGroups(List<String> keys,
                                    UserPreferencesService.FavoritesGroupingMode groupingMode,
                                    UserPreferencesService.FavoritesGroupOrderMode orderMode) {

        if (orderMode == UserPreferencesService.FavoritesGroupOrderMode.ALPHABETICAL) {
            // Countries: keep the special comparator. Custom: simple case-insensitive A–Z.
            if (groupingMode == UserPreferencesService.FavoritesGroupingMode.COUNTRY) {
                keys.sort(buildCountryCodeComparator());
            } else {
                keys.sort(String.CASE_INSENSITIVE_ORDER);
            }
            return keys;
        }

        // Manual ordering
        if (groupingMode == UserPreferencesService.FavoritesGroupingMode.COUNTRY) {
            return applyManualOrder(keys, preferencesService.getManualCountryGroupOrder(), buildCountryCodeComparator());
        }
        return applyManualOrder(keys, preferencesService.getCustomGroupNameOrder(), String.CASE_INSENSITIVE_ORDER);
    }

    private List<String> applyManualOrder(List<String> keys, List<String> manualOrder, Comparator<String> fallbackComparator) {
        List<String> ordered = new ArrayList<>();
        Set<String> remaining = new HashSet<>(keys);

        for (String k : manualOrder) {
            if (k == null) continue;
            String trimmed = k.trim();
            if (trimmed.isEmpty()) continue;

            // Match case-insensitively
            String actual = keys.stream().filter(x -> x.equalsIgnoreCase(trimmed)).findFirst().orElse(null);
            if (actual != null && remaining.remove(actual)) {
                ordered.add(actual);
            }
        }

        List<String> leftovers = new ArrayList<>(remaining);
        leftovers.sort(fallbackComparator);
        ordered.addAll(leftovers);

        return ordered;
    }

    // -------------------- Dialogs --------------------

    private void openReorderGroupsDialog() {
        UserPreferencesService.FavoritesGroupingMode groupingMode = preferencesService.getFavoritesGroupingMode();
        UserPreferencesService.FavoritesGroupOrderMode orderMode = preferencesService.getFavoritesGroupOrderMode();

        if (orderMode != UserPreferencesService.FavoritesGroupOrderMode.MANUAL) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Switch Order to Manual to reorder groups.", ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();
            return;
        }

        List<String> currentKeys;

        if (groupingMode == UserPreferencesService.FavoritesGroupingMode.COUNTRY) {
            currentKeys = favoritesService.getFavoriteEntries()
                    .stream()
                    .map(c -> normalizeCountryCode(c.getCountryCode()))
                    .distinct()
                    .collect(Collectors.toList());
            currentKeys.sort(buildCountryCodeComparator());

            List<String> startingOrder = applyManualOrder(new ArrayList<>(currentKeys),
                    preferencesService.getManualCountryGroupOrder(),
                    buildCountryCodeComparator());

            List<String> updated = showReorderListDialog("Reorder Country Groups", startingOrder);
            if (updated != null) {
                preferencesService.setManualCountryGroupOrder(updated);
            }
            return;
        }

        // CUSTOM grouping: reorder group names (including "Other" if it currently exists)
        Map<String, List<FavoriteCity>> grouped = groupByCustomGroups(favoritesService.getFavoriteEntries());
        currentKeys = new ArrayList<>(grouped.keySet());

        List<String> startingOrder = applyManualOrder(new ArrayList<>(currentKeys),
                preferencesService.getCustomGroupNameOrder(),
                String.CASE_INSENSITIVE_ORDER);

        List<String> updated = showReorderListDialog("Reorder Custom Groups", startingOrder);
        if (updated != null) {
            // Save only user-defined groups (exclude "Other" so it can always be appended if needed)
            List<String> save = updated.stream().filter(x -> !"Other".equalsIgnoreCase(x)).collect(Collectors.toList());
            preferencesService.setCustomGroupNameOrder(save);
        }
    }

    private List<String> showReorderListDialog(String title, List<String> items) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
        listView.getItems().addAll(items);
        listView.setPrefHeight(280);

        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button up = new Button("Up");
        Button down = new Button("Down");

        up.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx > 0) {
                String value = listView.getItems().remove(idx);
                listView.getItems().add(idx - 1, value);
                listView.getSelectionModel().select(idx - 1);
            }
        });

        down.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < listView.getItems().size() - 1) {
                String value = listView.getItems().remove(idx);
                listView.getItems().add(idx + 1, value);
                listView.getSelectionModel().select(idx + 1);
            }
        });

        buttons.getChildren().addAll(up, down);

        root.getChildren().addAll(new Label("Select a group then move it up/down:"), listView, buttons);
        dialog.getDialogPane().setContent(root);

        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) {
            return null;
        }
        return new ArrayList<>(listView.getItems());
    }

    private void openManageCustomGroupsDialog() {
        // Available country codes (from current favorites)
        List<String> available = favoritesService.getFavoriteEntries()
                .stream()
                .map(c -> normalizeCountryCode(c.getCountryCode()))
                .distinct()
                .sorted(buildCountryCodeComparator())
                .collect(Collectors.toList());

        List<UserPreferencesService.CustomCountryGroup> groups = new ArrayList<>(preferencesService.getCustomCountryGroups());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Custom Groups");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox root = new VBox(12);
        root.setPadding(new Insets(10));

        Label hint = new Label("Custom groups are made of country codes (e.g., \"SEA\" can contain PH, SG, MY).");
        hint.setWrapText(true);
        hint.getStyleClass().add("label-secondary");

        javafx.scene.control.ListView<String> groupList = new javafx.scene.control.ListView<>();
        groupList.getItems().addAll(groups.stream().map(UserPreferencesService.CustomCountryGroup::getName).collect(Collectors.toList()));
        groupList.setPrefHeight(180);

        VBox editorBox = new VBox(8);
        editorBox.setPadding(new Insets(8));
        editorBox.getStyleClass().add("forecast-card");

        Label selectedLabel = new Label("Select a group to edit its country codes.");
        selectedLabel.setWrapText(true);

        VBox checksBox = new VBox(6);

        Runnable refreshEditor = () -> {
            checksBox.getChildren().clear();
            int idx = groupList.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= groups.size()) {
                selectedLabel.setText("Select a group to edit its country codes.");
                return;
            }
            UserPreferencesService.CustomCountryGroup g = groups.get(idx);
            selectedLabel.setText("Editing: " + g.getName());

            Set<String> selected = g.getCountryCodes().stream().map(this::normalizeCountryCode).collect(Collectors.toSet());
            for (String code : available) {
                javafx.scene.control.CheckBox cb = new javafx.scene.control.CheckBox(code);
                cb.setSelected(selected.contains(code));
                cb.setOnAction(e -> {
                    Set<String> now = new HashSet<>(g.getCountryCodes().stream().map(this::normalizeCountryCode).collect(Collectors.toSet()));
                    if (cb.isSelected()) {
                        now.add(code);
                    } else {
                        now.remove(code);
                    }
                    groups.set(idx, new UserPreferencesService.CustomCountryGroup(g.getName(), new ArrayList<>(now)));
                });
                checksBox.getChildren().add(cb);
            }

            if (available.isEmpty()) {
                Label none = new Label("No country codes found yet. Add favorites with country codes first.");
                none.getStyleClass().add("label-secondary");
                checksBox.getChildren().add(none);
            }
        };

        groupList.getSelectionModel().selectedIndexProperty().addListener((obs, oldV, newV) -> refreshEditor.run());

        Button addGroup = new Button("Add");
        Button renameGroup = new Button("Rename");
        Button deleteGroup = new Button("Delete");

        addGroup.setOnAction(e -> {
            TextInputDialog input = new TextInputDialog();
            input.setTitle("New Group");
            input.setHeaderText("Create a custom group");
            input.setContentText("Group name:");
            input.showAndWait().ifPresent(name -> {
                String trimmed = name.trim();
                if (trimmed.isEmpty()) return;
                boolean exists = groups.stream().anyMatch(g -> g.getName().equalsIgnoreCase(trimmed));
                if (exists) return;
                groups.add(new UserPreferencesService.CustomCountryGroup(trimmed, new ArrayList<>()));
                groupList.getItems().add(trimmed);
                groupList.getSelectionModel().select(groupList.getItems().size() - 1);
                refreshEditor.run();
            });
        });

        renameGroup.setOnAction(e -> {
            int idx = groupList.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= groups.size()) return;

            UserPreferencesService.CustomCountryGroup g = groups.get(idx);

            TextInputDialog input = new TextInputDialog(g.getName());
            input.setTitle("Rename Group");
            input.setHeaderText("Rename custom group");
            input.setContentText("New name:");
            input.showAndWait().ifPresent(name -> {
                String trimmed = name.trim();
                if (trimmed.isEmpty()) return;
                boolean exists = groups.stream().anyMatch(x -> x.getName().equalsIgnoreCase(trimmed));
                if (exists && !g.getName().equalsIgnoreCase(trimmed)) return;

                groups.set(idx, new UserPreferencesService.CustomCountryGroup(trimmed, g.getCountryCodes()));
                groupList.getItems().set(idx, trimmed);
                refreshEditor.run();
            });
        });

        deleteGroup.setOnAction(e -> {
            int idx = groupList.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= groups.size()) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete the group \"" + groups.get(idx).getName() + "\"?",

                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            ButtonType result = confirm.showAndWait().orElse(ButtonType.NO);
            if (result != ButtonType.YES) return;

            groups.remove(idx);
            groupList.getItems().remove(idx);
            checksBox.getChildren().clear();
            selectedLabel.setText("Select a group to edit its country codes.");
        });

        HBox groupButtons = new HBox(8, addGroup, renameGroup, deleteGroup);
        groupButtons.setAlignment(Pos.CENTER_LEFT);

        editorBox.getChildren().addAll(selectedLabel, checksBox);

        root.getChildren().addAll(hint, new Label("Groups:"), groupList, groupButtons, new Separator(), editorBox);

        dialog.getDialogPane().setContent(root);

        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) {
            return;
        }

        preferencesService.setCustomCountryGroups(groups);

        // If user has no saved order, initialize it to current names.
        if (preferencesService.getCustomGroupNameOrder().isEmpty()) {
            preferencesService.setCustomGroupNameOrder(
                    groups.stream().map(UserPreferencesService.CustomCountryGroup::getName).collect(Collectors.toList())
            );
        }
    }

    // -------------------- Item Rendering --------------------

    private HBox createCityItem(FavoriteCity favoriteCity) {
        HBox itemBox = new HBox(8);
        itemBox.setAlignment(Pos.CENTER_LEFT);

        // Reuse a generic card style from CSS.
        itemBox.getStyleClass().add("forecast-card");

        Label cityLabel = new Label(favoriteCity.toDisplayString());
        cityLabel.getStyleClass().add("label-primary");
        cityLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cityLabel, Priority.ALWAYS);

        Button selectButton = new Button("Open");
        selectButton.getStyleClass().add("button-secondary");
        selectButton.setOnAction(e -> {
            if (onCitySelectCallback != null) {
                onCitySelectCallback.accept(favoriteCity.toSearchQuery());
            }
        });

        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("button-danger");
        removeButton.setOnAction(e -> {
            boolean removed = favoritesService.removeFavorite(favoriteCity.getCityName(), favoriteCity.getCountryCode());
            if (removed) {
                refreshFavorites();
                if (onFavoritesChangeCallback != null) {
                    onFavoritesChangeCallback.run();
                }
            }
        });

        itemBox.getChildren().addAll(cityLabel, selectButton, removeButton);
        return itemBox;
    }

    // -------------------- Helpers --------------------

    private Comparator<FavoriteCity> buildCityComparator() {
        return Comparator
                .comparing(FavoriteCity::getCityName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(FavoriteCity::getCountryCode, String.CASE_INSENSITIVE_ORDER);
    }

    private Comparator<String> buildCountryCodeComparator() {
        // Empty/unknown last, then A-Z.
        return (a, b) -> {
            String aa = normalizeCountryCode(a);
            String bb = normalizeCountryCode(b);

            boolean aBlank = aa.isBlank() || "??".equals(aa);
            boolean bBlank = bb.isBlank() || "??".equals(bb);

            if (aBlank && !bBlank) return 1;
            if (!aBlank && bBlank) return -1;

            return aa.compareToIgnoreCase(bb);
        };
    }

    private String normalizeCountryCode(String code) {
        if (code == null) {
            return "??";
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return "??";
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private UserPreferencesService.FavoritesGroupingMode parseGroupingMode(String value) {
        if (value != null && value.toLowerCase(Locale.ROOT).contains("custom")) {
            return UserPreferencesService.FavoritesGroupingMode.CUSTOM;
        }
        return UserPreferencesService.FavoritesGroupingMode.COUNTRY;
    }

    private UserPreferencesService.FavoritesGroupOrderMode parseOrderMode(String value) {
        if (value != null && value.toLowerCase(Locale.ROOT).contains("manual")) {
            return UserPreferencesService.FavoritesGroupOrderMode.MANUAL;
        }
        return UserPreferencesService.FavoritesGroupOrderMode.ALPHABETICAL;
    }
}
