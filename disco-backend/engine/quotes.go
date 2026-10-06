package engine

type SkillData struct {
	Name      string
	Quote     string
	AssetPath string
}

func InitDiscoElysiumQuotes() map[string]string {
	return map[string]string{
		// --- INTELLECT ---
		"logic":             `Do it for the picture puzzle. Put it all together. Solve the world. One conversation at a time.`,
		"encyclopedia":      `Your mangled brain would like you to know there is a boxer called Contact Mike.`,
		"rhetoric":          `Yes! Abject failure. Total, irreversible defeat on all fronts! Absolutely vanquished, beaten, curb-stomped and pissed on — until *you* came along! You alone, against every living thing, against every human alive: eight hundred trillion reál in the hands of an *impossibly* well organized ruling class. You are The Last Communist. Now get to work, comrade.`,
		"drama":             `Everyone knows you can't trust artists. They're nearly as bad as clowns.`,
		"conceptualization": `The world’s most precious material, reserved for those she lets close enough to feel it. You are stealing a touch. It’s not yours to take.`,
		"visual_calculus":   `The man does not know the bullet has entered his brain. He never will. Death comes faster than the realization.`,

		// --- PSYCHE ---
		"volition":        `No. This is somewhere to be. This is all you have, but it’s still something. Streets and sodium lights. The sky, the world. You’re still alive.`,
		"inland_empire":   `A tremendous loneliness comes over you. Everybody in the world is doing something without you.`,
		"empathy":         `This is a very, very sad man who has just seen something that’s made him forget his sadness.`,
		"authority":       `And you? Is it an honour to work with you? Don't ask him, ask yourself.`,
		"suggestion":      `This was not about failure or success. This was always going to be horror. I should not have suggested it, and you should not have listened to me.`,
		"esprit_de_corps": `If an assault were launched on this building right now — if the windows came crashing down and the whole world descended upon you — this man would hurl himself in death's way to save you.`,

		// --- PHYSIQUE ---
		"endurance":           `Think about the seagull's story. It's one of endurance -- and adaptation. The seaside was paradise once. Then their paradise became *shit city*. And what did they do? They became urban survivors! Eating burgers out of trash cans! No time for sentimental bullshit. Just like you.`,
		"pain_threshold":      `There's tenderness in the carabineer's look. Tenderness that's curdled into pain or something darker. Even worse, a love aborted and smothered, stamped beneath his brilliant boot heel.`,
		"physical_instrument": `Cold and heavy -- like truth.`,
		"electrochemistry":    `The *funk soul brother* at the back of his head has gone dark. Forever.`,
		"shivers":             `I am a fragment of the world spirit, the genius loci of Revachol. I've seen you, I've seen you! I've seen you with her — and I've seen you without her. I've seen you on the crescent of the hill.`,
		"half_light":          `He whispers with such predatory hunger it borders on *longing*.`,

		// --- MOTORICS (New Additions) ---
		"hand_eye_coordination": `A gun is a tool for putting holes into things far away. Sometimes those things are human heads. Keep your fingers steady.`,
		"perception":            `Listen closely. The silence here is not empty; it is crowded with things people decided not to say.`,
		"reaction_speed":        `The second ticks by like a falling guillotine blade. Move, or be severed.`,
		"savoir_faire":          `Style is not an accident. It is a calculated declaration of war against the mundane.`,
		"interfacing":           `The needle drops into the groove. Friction, vibration, amplification. You understand machines because they are the only things that tell the simple truth.`,
		"composure":             `Straighten your collar. Swallow the bile. No one can know that your heart is coming apart at the seams.`,

		// --- SPECIAL CHECKS ---
		"snake_eyes": `CRITICAL FAILURE: Two ones stare back at you like empty eye sockets. The universe simply refuses to cooperate.`,
		"boxcars":    `CRITICAL SUCCESS: Double sixes. Pure, unadulterated transcendence. You could split an atom with your bare grin.`,
	}
}

func GetAllSkills() []SkillData {
	quotes := InitDiscoElysiumQuotes()
	skills := []SkillData{
		{Name: "LOGIC", Quote: quotes["logic"], AssetPath: "Intellect/Logic.jpg"},
		{Name: "ENCYCLOPEDIA", Quote: quotes["encyclopedia"], AssetPath: "Intellect/Encyclopedia.jpg"},
		{Name: "RHETORIC", Quote: quotes["rhetoric"], AssetPath: "Intellect/Rhetoric.jpg"},
		{Name: "DRAMA", Quote: quotes["drama"], AssetPath: "Intellect/Drama.jpg"},
		{Name: "CONCEPTUALIZATION", Quote: quotes["conceptualization"], AssetPath: "Intellect/Conceptualization.jpg"},
		{Name: "VISUAL CALCULUS", Quote: quotes["visual_calculus"], AssetPath: "Intellect/Visual_Calculus.jpg"},
		{Name: "VOLITION", Quote: quotes["volition"], AssetPath: "Psyche/Volition.jpg"},
		{Name: "INLAND EMPIRE", Quote: quotes["inland_empire"], AssetPath: "Psyche/Inland_Empire.jpg"},
		{Name: "EMPATHY", Quote: quotes["empathy"], AssetPath: "Psyche/Empathy.jpg"},
		{Name: "AUTHORITY", Quote: quotes["authority"], AssetPath: "Psyche/Authority.jpg"},
		{Name: "SUGGESTION", Quote: quotes["suggestion"], AssetPath: "Psyche/Suggestion.jpg"},
		{Name: "ESPRIT DE CORPS", Quote: quotes["esprit_de_corps"], AssetPath: "Psyche/Espirit_De_Corps.jpg"},
		{Name: "ENDURANCE", Quote: quotes["endurance"], AssetPath: "Physique/Endurance.jpg"},
		{Name: "PAIN THRESHOLD", Quote: quotes["pain_threshold"], AssetPath: "Physique/Pain_Threshold.jpg"},
		{Name: "PHYSICAL INSTRUMENT", Quote: quotes["physical_instrument"], AssetPath: "Physique/Physical_Instrument.jpg"},
		{Name: "ELECTROCHEMISTRY", Quote: quotes["electrochemistry"], AssetPath: "Physique/Electrochemistry.jpg"},
		{Name: "SHIVERS", Quote: quotes["shivers"], AssetPath: "Physique/Shivers.jpg"},
		{Name: "HALF LIGHT", Quote: quotes["half_light"], AssetPath: "Physique/Half_Light.jpg"},
		{Name: "HAND/EYE COORDINATION", Quote: quotes["hand_eye_coordination"], AssetPath: "Motorics/Hand_Eye_Coordination.jpg"},
		{Name: "PERCEPTION", Quote: quotes["perception"], AssetPath: "Motorics/Perception.jpg"},
		{Name: "REACTION SPEED", Quote: quotes["reaction_speed"], AssetPath: "Motorics/Reaction_Speed.jpg"},
		{Name: "SAVOIR FAIRE", Quote: quotes["savoir_faire"], AssetPath: "Motorics/Savoir_Faire.jpg"},
		{Name: "INTERFACING", Quote: quotes["interfacing"], AssetPath: "Motorics/Interfacing.jpg"},
		{Name: "COMPOSURE", Quote: quotes["composure"], AssetPath: "Motorics/Composure.jpg"},
	}
	return skills
}
