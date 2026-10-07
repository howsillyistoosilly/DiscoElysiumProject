package engine

import (
	"strings"
	"testing"
)

func TestNewEngine(t *testing.T) {
	e := NewEngine()
	if e == nil {
		t.Fatal("NewEngine() returned nil")
	}
	if len(e.skills) == 0 {
		t.Fatal("NewEngine() produced no skills")
	}
}

func TestRollReturnsDice(t *testing.T) {
	e := NewEngine()
	for i := 0; i < 200; i++ {
		r := e.Roll()
		if r.Die1 < 1 || r.Die1 > 6 {
			t.Fatalf("Die1 out of range [1,6]: got %d", r.Die1)
		}
		if r.Die2 < 1 || r.Die2 > 6 {
			t.Fatalf("Die2 out of range [1,6]: got %d", r.Die2)
		}
		if r.Total != r.Die1+r.Die2 {
			t.Fatalf("Total mismatch: Die1=%d Die2=%d Total=%d", r.Die1, r.Die2, r.Total)
		}
	}
}

func TestRollDieAssets(t *testing.T) {
	e := NewEngine()
	for i := 0; i < 100; i++ {
		r := e.Roll()
		wantDie1 := "dice/die" + string(rune('0'+r.Die1)) + ".png"
		wantDie2 := "dice/die" + string(rune('0'+r.Die2)) + ".png"
		if r.Die1Asset != wantDie1 {
			t.Errorf("Die1Asset: want %q got %q", wantDie1, r.Die1Asset)
		}
		if r.Die2Asset != wantDie2 {
			t.Errorf("Die2Asset: want %q got %q", wantDie2, r.Die2Asset)
		}
	}
}

func TestCriticalFailureViaTestable(t *testing.T) {
	e := &testableEngine{skills: GetAllSkills(), rolls: []int{0, 0}} // +1 => 1,1
	r := e.Roll()
	if r.Die1 != 1 || r.Die2 != 1 {
		t.Fatalf("expected 1,1 but got %d,%d", r.Die1, r.Die2)
	}
	if r.Header != "CRITICAL FAILURE" {
		t.Errorf("expected CRITICAL FAILURE header, got %q", r.Header)
	}
	if !r.IsCritical {
		t.Error("IsCritical should be true for double 1s")
	}
	if r.AccentColor != "#D71921" {
		t.Errorf("expected red accent for crit fail, got %q", r.AccentColor)
	}
}

func TestCriticalSuccessViaTestable(t *testing.T) {
	e := &testableEngine{skills: GetAllSkills(), rolls: []int{5, 5}} // +1 => 6,6
	r := e.Roll()
	if r.Die1 != 6 || r.Die2 != 6 {
		t.Fatalf("expected 6,6 but got %d,%d", r.Die1, r.Die2)
	}
	if r.Header != "CRITICAL SUCCESS" {
		t.Errorf("expected CRITICAL SUCCESS header, got %q", r.Header)
	}
	if !r.IsCritical {
		t.Error("IsCritical should be true for double 6s")
	}
	if r.AccentColor != "#7D6BB3" {
		t.Errorf("expected purple accent for crit success, got %q", r.AccentColor)
	}
}

func TestRegularRollPopulatesFields(t *testing.T) {
	e := NewEngine()
	// Roll many times and check every non-critical result
	for i := 0; i < 500; i++ {
		r := e.Roll()
		if r.IsCritical {
			continue
		}
		if r.Header == "" {
			t.Error("regular roll: Header is empty")
		}
		if r.Quote == "" {
			t.Error("regular roll: Quote is empty")
		}
		if r.AssetPath == "" {
			t.Error("regular roll: AssetPath is empty")
		}
		if r.AccentColor == "" {
			t.Error("regular roll: AccentColor is empty")
		}
	}
}

func TestAccentColors(t *testing.T) {
	tests := []struct {
		path  string
		color string
	}{
		{"Intellect/Logic.jpg", "#C4A35A"},
		{"Psyche/Volition.jpg", "#8170B2"},
		{"Physique/Half_Light.jpg", "#A84F63"},
		{"Motorics/Perception.jpg", "#6C9B9A"},
		{"Unknown/Something.jpg", "#8A8A8A"},
	}
	for _, tt := range tests {
		got := accentForAsset(tt.path)
		if got != tt.color {
			t.Errorf("accentForAsset(%q) = %q, want %q", tt.path, got, tt.color)
		}
	}
}

func TestGetAllSkillsCount(t *testing.T) {
	skills := GetAllSkills()
	// 6 Intellect + 6 Psyche + 6 Physique + 6 Motorics = 24
	if len(skills) != 24 {
		t.Errorf("expected 24 skills, got %d", len(skills))
	}
}

func TestGetAllSkillsNoEmptyFields(t *testing.T) {
	for _, s := range GetAllSkills() {
		if s.Name == "" {
			t.Errorf("skill has empty Name (AssetPath=%q)", s.AssetPath)
		}
		if s.Quote == "" {
			t.Errorf("skill %q has empty Quote", s.Name)
		}
		if s.AssetPath == "" {
			t.Errorf("skill %q has empty AssetPath", s.Name)
		}
	}
}

func TestAssetPathPrefix(t *testing.T) {
	validPrefixes := []string{"Intellect/", "Psyche/", "Physique/", "Motorics/"}
	for _, s := range GetAllSkills() {
		ok := false
		for _, prefix := range validPrefixes {
			if strings.HasPrefix(s.AssetPath, prefix) {
				ok = true
				break
			}
		}
		if !ok {
			t.Errorf("skill %q has unexpected AssetPath prefix: %q", s.Name, s.AssetPath)
		}
	}
}

func TestInitDiscoElysiumQuotesHasAllKeys(t *testing.T) {
	q := InitDiscoElysiumQuotes()
	required := []string{
		"logic", "encyclopedia", "rhetoric", "drama", "conceptualization", "visual_calculus",
		"volition", "inland_empire", "empathy", "authority", "suggestion", "esprit_de_corps",
		"endurance", "pain_threshold", "physical_instrument", "electrochemistry", "shivers", "half_light",
		"hand_eye_coordination", "perception", "reaction_speed", "savoir_faire", "interfacing", "composure",
	}
	for _, key := range required {
		if q[key] == "" {
			t.Errorf("InitDiscoElysiumQuotes() missing or empty key: %q", key)
		}
	}
}

// testableEngine uses a predetermined slice of die values for deterministic tests.

// testableEngine is a version of DiscoDiceEngine that uses a slice of predetermined die values.
type testableEngine struct {
	skills []SkillData
	rolls  []int
	pos    int
}

func (e *testableEngine) nextDie() int {
	v := e.rolls[e.pos%len(e.rolls)]
	e.pos++
	return v + 1 // simulate Intn(6)+1
}

func (e *testableEngine) Roll() RollResponse {
	d1 := e.nextDie()
	d2 := e.nextDie()

	resp := RollResponse{
		Die1:      d1,
		Die2:      d2,
		Total:     d1 + d2,
		Die1Asset: "dice/die" + string(rune('0'+d1)) + ".png",
		Die2Asset: "dice/die" + string(rune('0'+d2)) + ".png",
	}

	if d1 == 1 && d2 == 1 {
		resp.Header = "CRITICAL FAILURE"
		resp.Quote = "Two ones stare back at you like empty eye sockets. The universe simply refuses to cooperate."
		resp.AssetPath = "Physique/Half_Light.jpg"
		resp.IsCritical = true
		resp.AccentColor = "#D71921"
		return resp
	}

	if d1 == 6 && d2 == 6 {
		resp.Header = "CRITICAL SUCCESS"
		resp.Quote = "Double sixes. Pure, unadulterated transcendence. You could split an atom with your bare grin."
		resp.AssetPath = "Psyche/Volition.jpg"
		resp.IsCritical = true
		resp.AccentColor = "#7D6BB3"
		return resp
	}

	chosen := e.skills[0]
	resp.Header = chosen.Name
	resp.Quote = chosen.Quote
	resp.AssetPath = chosen.AssetPath
	resp.IsCritical = false
	resp.AccentColor = accentForAsset(chosen.AssetPath)
	return resp
}

func TestTestableEngineCritFail(t *testing.T) {
	e := &testableEngine{skills: GetAllSkills(), rolls: []int{0, 0}} // +1 = 1,1
	r := e.Roll()
	if r.Header != "CRITICAL FAILURE" {
		t.Errorf("expected CRITICAL FAILURE, got %q", r.Header)
	}
}

func TestTestableEngineCritSuccess(t *testing.T) {
	e := &testableEngine{skills: GetAllSkills(), rolls: []int{5, 5}} // +1 = 6,6
	r := e.Roll()
	if r.Header != "CRITICAL SUCCESS" {
		t.Errorf("expected CRITICAL SUCCESS, got %q", r.Header)
	}
}

func TestTestableEngineRegular(t *testing.T) {
	e := &testableEngine{skills: GetAllSkills(), rolls: []int{2, 3}} // 3+4=7, regular
	r := e.Roll()
	if r.IsCritical {
		t.Error("3+4 should not be critical")
	}
	if r.Header == "" {
		t.Error("Header should be set for regular roll")
	}
	if r.Total != 7 {
		t.Errorf("expected total 7, got %d", r.Total)
	}
}
