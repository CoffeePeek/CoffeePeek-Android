# UI inventory and extraction decisions

Scope: registered Gradle modules, Compose symbols/imports, component declarations,
resources, Modifier extensions and call sites. Current UI is in composeApp
(commonMain plus Android entry points/platform theme hooks). Legacy domain/data/
network/room and existing core modules contain no Compose UI components.
This is a code/dependency inventory, not a screen-by-screen visual parity audit.

The component directory has 30 files declaring composables. Shared style and icon
imports extend into feature screens, so folder location alone is not ownership.

| Family / current source in composeApp | Evidence / ownership | Preparation |
|---|---|---|
| theme/Colors, Dimens, CoffeePeekTheme, Typography | Shared by screens and primitives; Manrope currently uses app resources | Shared subset prepared; feature decoration excluded; font injected |
| ui/icons/CpIcons | Common navigation/actions/status symbols, reused across screens | Facade prepared, vendor hidden |
| component/AppButton and Buttons.Common | Same brand pill/height; AppButton used in add-shop, review, roaster and report screens | AppButton prepared; reconcile width/layout differences at integration |
| component/LiquidGlass | Modifier used in map, shop, auth and floating navigation | Modifier/local/glass control prepared; screen owns Haze source |
| component/GroupedList | GroupSection/CheckmarkRow/ActionRow/separators reused in filter and form UIs | Generic rows prepared; CatalogItem selection/expansion remains outside core |
| AppTextField, CompactOutlinedTextField, CpSearchField | Overlapping field style, content padding, icons; keyboard/focus and labels differ | Prepared: hoisted state, keyboard contracts, caller labels and error/disabled semantics |
| CpTopBar, Buttons.BackButton, FloatingBottomNavBar | CpTopBar imports Navigator; root destinations and selection are app composition | Stateless CpTopBar/back prepared; root navigation remains outside core |
| SettingsList, SettingsIconBadge, CapsuleSegmentedControl | Visual row/badge/selection primitives with repeated radii and typography | Neutral badge/palette and rows/selection prepared; version footer excluded; enabled/selection contracts tested |
| DescriptionCard, ClickableAnnotatedText, Texts | Presentation helpers; text wrappers alone may not justify new public APIs | Consolidate on theme typography; preserve link handling with caller callbacks |
| CoffeePeekLoader, CoffeePeekPullToRefresh, LoadingDialog, ErrorDialog | Loading/error primitives; animation, action and lifecycle behaviours differ | Loader/error prepared; loading modal has explicit non-dismissible contract; refresh gesture/cooldown requires separate slice |
| SwipeDismissModalBottomSheet, FabMenu | Sheet reused by review/check-in/photo source; FAB currently has no consumers | Handle-only sheet and neutral FAB prepared; accessible dismiss, callback/disabled and drag/back tests; no screen state |
| Insets | Shared safe-area padding; depends on SizeObserver and hardcoded Ltr | Rewrite using direct density and layout direction; verify RTL; do not copy its coupling |
| PhotoViewer, FullScreenImageDialog, SwipeablePhotoStack, PhotoSourceBottomSheet, PhotoAttachmentsSection | Image loading, zoom, selection and platform picking must be separated | Generic gallery rendering may move later; platform picker and feature attachments do not |
| ReviewContent, CityCatalogChips, BrewMethodIcon, PriceBeanSlider, GuestAuthCard | Review/CheckIn/City models, feature-specific labels/assets/business affordances | Remain feature-owned; split neutral layout only if actual reuse warrants it |
| CoffeeShopPlaceholderImage and resources | Product imagery/image-loader policy vs generic rendering | Decide resource/image-loading ownership separately; no DTO/image repository in core |

## Modifiers and inline repetition

Only two explicitly named Modifier extensions exist in the legacy UI inventory:
public `liquidGlass` and private `PriceBeanSlider.bynIconSize`. The latter handles
a currency-specific visual ratio and is not a general design-system utility.
Inline rounded-card/circle/pill clipping and padding repeat in feed, profile,
reviews, map and contribution screens. Prefer shared tokens/shapes/components
before introducing a modifier for every repeated chain. Navigation/IME insets,
gesture interception and map interaction stay with their owning UI boundary.

## Resource decisions

Manrope fonts, brand logos, mascots and Google/brew/rating/currency drawables are
currently app resources. Fonts/logos are candidates for a dedicated resource
slice; review/check-in ratings, brewing and auth artwork are not automatically
shared design-system assets. No assets are moved merely to make core look complete.
Directional icon mirroring and semantic descriptions require RTL/accessibility
verification during integration; callers provide descriptions for actionable icons.

## Next slices

1. Fields/search and generic top bars prepared independently; integrate later.
2. Badges/segmented controls, loading/error and sheet/FAB prepared. Refresh
   gesture/cooldown remains a separate slice. Sheet keyboard/insets/scrolling QA
   remains part of consumer integration.
3. Resource ownership and brand-font packaging; safe RTL-aware insets.
4. Separate integration PRs for consumer families, visual parity checks and legacy
   removal. No migrated feature should add new code to legacy components.

Android ComponentPreviews provides light/dark samples for every prepared family.
Modal samples need Interactive/Run Preview; fixtures do not depend on the app.
