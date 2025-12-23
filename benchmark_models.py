import requests
import time
import json

# OpenRouter API configuration
API_KEY = "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"
API_URL = "https://openrouter.ai/api/v1/chat/completions"

# Free models to test (as of Dec 2024)
FREE_MODELS = [
    "meta-llama/llama-3.3-70b-instruct:free",
    "meta-llama/llama-3.1-8b-instruct:free",
    "google/gemini-flash-1.5:free",
    "google/gemini-2.0-flash-exp:free",
    "mistralai/mistral-7b-instruct:free",
    "nousresearch/hermes-3-llama-3.1-405b:free",
    "qwen/qwen-2-7b-instruct:free",
]

# Test prompt (typical interview question validation)
SYSTEM_PROMPT = """You are a helpful interview coach. Evaluate the candidate's answer and provide brief, direct feedback. 
Speak directly to the candidate using 'you' and 'your'. Be encouraging but honest. 
Keep feedback concise (max 2-3 sentences per section). 
Format response as JSON: {"score": <0-100>, "strengths": "<what you did well>", "improvements": "<what you should improve>"}"""

USER_PROMPT = """Question: What does 'loose coupling' mean?

Correct Answer: Low dependency

Candidate's Answer: It means components are independent and don't rely heavily on each other

Evaluate this answer. Give a score (0-100) and brief, direct feedback."""

def test_model(model_name):
    """Test a single model and return response time and result"""
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json",
        "HTTP-Referer": "http://localhost:8080",
        "X-Title": "InterviewAI-Benchmark"
    }
    
    payload = {
        "model": model_name,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": USER_PROMPT}
        ],
        "temperature": 0.3,
        "max_tokens": 500
    }
    
    try:
        print(f"\n🔄 Testing: {model_name}")
        start_time = time.time()
        
        response = requests.post(API_URL, headers=headers, json=payload, timeout=30)
        
        end_time = time.time()
        duration = end_time - start_time
        
        if response.status_code == 200:
            data = response.json()
            
            # Extract content
            content = None
            if "choices" in data and len(data["choices"]) > 0:
                content = data["choices"][0]["message"]["content"]
            
            # Try to parse score
            score = None
            if content:
                try:
                    # Remove markdown code blocks
                    clean_content = content.replace("```json", "").replace("```", "").strip()
                    parsed = json.loads(clean_content)
                    score = parsed.get("score", "N/A")
                except:
                    pass
            
            return {
                "model": model_name,
                "success": True,
                "duration": duration,
                "status_code": response.status_code,
                "score": score,
                "content_preview": content[:100] if content else "No content"
            }
        else:
            return {
                "model": model_name,
                "success": False,
                "duration": duration,
                "status_code": response.status_code,
                "error": response.text[:200]
            }
            
    except Exception as e:
        return {
            "model": model_name,
            "success": False,
            "duration": 0,
            "error": str(e)
        }

def main():
    print("=" * 80)
    print("🚀 OpenRouter Model Benchmark - Free Models")
    print("=" * 80)
    print(f"\nTesting {len(FREE_MODELS)} free models...")
    print("This will take a few minutes...\n")
    
    results = []
    
    for model in FREE_MODELS:
        result = test_model(model)
        results.append(result)
        
        if result["success"]:
            print(f"✅ Success! Duration: {result['duration']:.2f}s | Score: {result.get('score', 'N/A')}")
        else:
            print(f"❌ Failed: {result.get('error', 'Unknown error')[:100]}")
        
        # Small delay between requests to avoid rate limiting
        time.sleep(1)
    
    # Sort by duration (successful ones first)
    successful = [r for r in results if r["success"]]
    failed = [r for r in results if not r["success"]]
    successful.sort(key=lambda x: x["duration"])
    
    # Print summary
    print("\n" + "=" * 80)
    print("📊 BENCHMARK RESULTS - SORTED BY SPEED")
    print("=" * 80)
    
    if successful:
        print("\n✅ SUCCESSFUL MODELS (fastest to slowest):\n")
        print(f"{'Rank':<6} {'Model':<50} {'Duration':<12} {'Score':<8}")
        print("-" * 80)
        
        for i, result in enumerate(successful, 1):
            model_short = result["model"].split("/")[-1][:45]
            duration_str = f"{result['duration']:.2f}s"
            score_str = str(result.get('score', 'N/A'))
            
            # Highlight the fastest
            if i == 1:
                print(f"🥇 {i:<4} {model_short:<50} {duration_str:<12} {score_str:<8}")
            elif i == 2:
                print(f"🥈 {i:<4} {model_short:<50} {duration_str:<12} {score_str:<8}")
            elif i == 3:
                print(f"🥉 {i:<4} {model_short:<50} {duration_str:<12} {score_str:<8}")
            else:
                print(f"   {i:<4} {model_short:<50} {duration_str:<12} {score_str:<8}")
    
    if failed:
        print(f"\n\n❌ FAILED MODELS ({len(failed)}):\n")
        for result in failed:
            model_short = result["model"].split("/")[-1][:45]
            error = result.get("error", "Unknown")[:50]
            print(f"   • {model_short}: {error}")
    
    # Recommendation
    if successful:
        fastest = successful[0]
        print("\n" + "=" * 80)
        print("💡 RECOMMENDATION")
        print("=" * 80)
        print(f"\n🏆 Fastest Model: {fastest['model']}")
        print(f"⏱️  Average Response Time: {fastest['duration']:.2f} seconds")
        print(f"📊 Test Score: {fastest.get('score', 'N/A')}/100")
        print(f"\n✨ Update your config.properties with:")
        print(f"   openrouter.model={fastest['model']}")
    
    print("\n" + "=" * 80)

if __name__ == "__main__":
    main()
