package engine

import (
	"math/rand"
	"strings"
	"time"
)

type RollResponse struct {
	Die1        int    `json:"die1"`
	Die2        int    `json:"die2"`
	Total       int    `json:"total"`
	Header      string `json:"header"`
	Quote       string `json:"quote"`
	AssetPath   string `json:"asset_path"`
	IsCritical  bool   `json:"is_critical"`
	Die1Asset   string `json:"die1_asset"`
	Die2Asset   string `json:"die2_asset"`
	AccentColor string `json:"accent_color"`
}

type DiscoDiceEngine struct {
	skills []SkillData
	rng    *rand.Rand
}

func NewEngine() *DiscoDiceEngine {
	return &DiscoDiceEngine{
		skills: GetAllSkills(),
		rng:    rand.New(rand.NewSource(time.Now().UnixNano())),
	}
}

func (e *DiscoDiceEngine) Roll() RollResponse {
	d1 := e.rng.Intn(6) + 1
	d2 := e.rng.Intn(6) + 1

	resp := RollResponse{
		Die1:      d1,
		Die2:      d2,
		Total:     d1 + d2,
		Die1Asset: "dice/die" + string(rune('0'+d1)) + ".png",
		Die2Asset: "dice/die" + string(rune('0'+d2)) + ".png",
	}

	// Double 1s: Critical Failure
	if d1 == 1 && d2 == 1 {
		resp.Header = "CRITICAL FAILURE"
		resp.Quote = "Two ones stare back at you like empty eye sockets. The universe simply refuses to cooperate."
		resp.AssetPath = "Physique/Half_Light.jpg"
		resp.IsCritical = true
		resp.AccentColor = "#D71921"
		return resp
	}

	// Double 6s: Critical Success
	if d1 == 6 && d2 == 6 {
		resp.Header = "CRITICAL SUCCESS"
		resp.Quote = "Double sixes. Pure, unadulterated transcendence. You could split an atom with your bare grin."
		resp.AssetPath = "Psyche/Volition.jpg"
		resp.IsCritical = true
		resp.AccentColor = "#7D6BB3"
		return resp
	}

	// Regular rolls: choose random skill quote
	chosen := e.skills[e.rng.Intn(len(e.skills))]
	resp.Header = chosen.Name
	resp.Quote = chosen.Quote
	resp.AssetPath = chosen.AssetPath
	resp.IsCritical = false
	resp.AccentColor = accentForAsset(chosen.AssetPath)

	return resp
}

func accentForAsset(assetPath string) string {
	switch {
	case strings.HasPrefix(assetPath, "Intellect/"):
		return "#C4A35A"
	case strings.HasPrefix(assetPath, "Psyche/"):
		return "#8170B2"
	case strings.HasPrefix(assetPath, "Physique/"):
		return "#A84F63"
	case strings.HasPrefix(assetPath, "Motorics/"):
		return "#6C9B9A"
	default:
		return "#8A8A8A"
	}
}
