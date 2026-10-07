package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"

	"disco-sorter/disco-backend/engine"
)

func main() {
	disco := engine.NewEngine()
	assetDir := findDirectory("disco-backend/assets", "assets")
	webDir := findDirectory("web", "../web")

	withHeaders := func(handler http.HandlerFunc) http.HandlerFunc {
		return func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("Access-Control-Allow-Origin", "*")
			w.Header().Set("Access-Control-Allow-Methods", "GET, OPTIONS")
			w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
			if r.Method == http.MethodOptions {
				w.WriteHeader(http.StatusNoContent)
				return
			}
			if r.Method != http.MethodGet {
				http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
				return
			}
			handler(w, r)
		}
	}

	http.HandleFunc("/api/health", withHeaders(func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, map[string]string{"status": "ok"})
	}))
	http.HandleFunc("/api/roll", withHeaders(func(w http.ResponseWriter, r *http.Request) {
		result := disco.Roll()
		if err := writeJSON(w, result); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
		}
	}))

	fs := http.FileServer(http.Dir(assetDir))
	http.Handle("/assets/", http.StripPrefix("/assets/", fs))
	http.HandleFunc("/dashboard", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "android/dashboard.html")
	})
	http.Handle("/", http.FileServer(http.Dir(webDir)))

	port := ":8080"
	log.Printf("Disco Elysium widget server online at http://localhost%s (assets: %s)", port, assetDir)
	log.Fatal(http.ListenAndServe(port, nil))
}

func findDirectory(paths ...string) string {
	for _, path := range paths {
		if info, err := os.Stat(path); err == nil && info.IsDir() {
			return path
		}
	}
	log.Fatal("required directory not found: " + fmt.Sprint(paths))
	return ""
}

func writeJSON(w http.ResponseWriter, value any) error {
	w.Header().Set("Content-Type", "application/json")
	return json.NewEncoder(w).Encode(value)
}
