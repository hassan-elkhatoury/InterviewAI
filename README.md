# InterviewAI - Your Personal Interview Coach 🎤

**InterviewAI** is a modern, professional interview preparation platform that combines structured learning paths with AI-powered feedback to help you master interviews with confidence.

## ✨ Key Features

### 🏠 **Clean Home Dashboard**
- **Next Lesson Card**: See your next challenge at a glance
- **Learning Path Visualization**: Track your journey through 6 stages
- **AI Coach Widget**: Get personalized motivational messages
- **Quick Actions**: Start mock interviews, review feedback instantly

### 📚 **Structured Learning Path** (6 Stages)
1. ✅ **Self-Introduction** - Master your first impression
2. ✅ **Strengths** - Confidently showcase your abilities  
3. ⏳ **Weaknesses** - Turn challenges into growth opportunities
4. ⭕ **Technical Questions** - Navigate technical discussions
5. ⭕ **HR Questions** - Excel in behavioral interviews
6. ⭕ **Final Mock Interview** - Full simulation experience

### 🎮 **Gamification System**
- **XP Points**: Earn points for lessons, mocks, streaks
- **Streaks**: 🔥 Track daily consistency
- **Badges**: Unlock achievements like "Consistency Hero", "Communication Master"
- **Daily Missions**: Small daily tasks (+5-15 XP each)
- **Leaderboard**: Compete with learners (optional)

### 📊 **Progress Tracking**
- Real-time XP and level progression
- Lessons completed counter
- Interview categories mastered
- Performance analytics

## 🚀 Quick Start

### Prerequisites
- Java 11+
- Maven 3.9+
- MySQL 8.0+

### Installation

```bash
# Clone repository
git clone https://github.com/hassan-elkhatoury/InterviewAI.git
cd InterviewAI

# Set up database
mysql -u root -p < src/main/resources/db/schema.sql

# Configure database
# Edit: src/main/resources/config/config.properties
# Set your: db.host, db.user, db.password

# Build and run
mvn clean install
mvn javafx:run
```

### Demo Account
- **Username**: `admin`
- **Password**: `password`

## 🎨 Dashboard Layout

```
┌──────────────────────────────────────────────────────────┐
│ 🏠 Home  📈 Progress  🎯 Quests  🏅 Leaderboard  👤 ⚙️    │
├──────────────────────┬─────────────────────────────────────┤
│                      │                                     │
│  SIDEBAR             │  ┌──────────────────────────┐       │
│  🏠 Home/Learn       │  │ NEXT LESSON CARD         │ 🕒    │
│  📈 Progress         │  ├──────────────────────────┤ DAI   │
│  🎯 Quests           │  │ "Behavioral Q&A..."      │ LY    │
│  🏅 Leaderboard      │  │ [Start/Continue]         │ MIS   │
│  👤 Profile          │  └──────────────────────────┘ SION  │
│  ⚙️ Settings          │                                     │
│                      │  ┌──────────────────────────┐ S    │
│                      │  │ LEARNING PATH            │ T    │
│                      │  ├──────────────────────────┤ A    │
│                      │  │ ✅ Self-Introduction     │ T    │
│                      │  │ ✅ Strengths             │ S    │
│                      │  │ ⏳ Weaknesses            │      │
│                      │  │ ⭕ Technical             │      │
│                      │  │ ⭕ HR Questions          │      │
│                      │  │ ⭕ Final Mock            │      │
│                      │  └──────────────────────────┘      │
│                      │                                     │
│                      │  ┌──────────────────────────┐       │
│                      │  │ AI COACH WIDGET          │       │
│                      │  ├──────────────────────────┤       │
│                      │  │ "You're doing great!     │       │
│                      │  │  Keep the streak! 💪"    │       │
│                      │  └──────────────────────────┘       │
│                      │                                     │
│                      │ 🎤 📘 🎥 (Quick Actions)            │
└──────────────────────┴─────────────────────────────────────┘
```

## 🎨 Design System

**Color Palette**:
- **Background**: #0E1014 or #111827 (Professional dark)
- **Cards**: #1a2844 (Deep blue)
- **Primary Accent**: #22C55E (Fresh green)
- **Secondary Accent**: #3B82F6 (Modern blue)
- **Text**: #e2e8f0 (Light) | #94a3b8 (Muted)

**Typography**:
- Font: Poppins / Inter / Segoe UI
- Modern, clean readability

**Layout**:
- Card-based design
- Rounded corners: 16-24px radius
- Subtle hover & progress animations

## 🛠️ Tech Stack

| Component | Version |
|-----------|---------|
| Frontend | JavaFX 20 |
| Backend | Java 11+ |
| Database | MySQL 8.0+ |
| Build Tool | Maven 3.9+ |
| Security | BCrypt |

## 📖 Documentation

See full documentation in these files:
- **[PROJECT_DOCUMENTATION.md](./PROJECT_DOCUMENTATION.md)** - Complete technical reference
- **[IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)** - Step-by-step implementation
- **[howto.md](./howto.md)** - How-to guides

## 🗄️ Database Schema

**Core Tables**:
- `users` - User accounts
- `progress` - XP, levels, streaks
- `learning_paths` - 6 learning stages
- `lessons` - Lesson content
- `questions` - Interview questions
- `feedback` - User responses & AI feedback
- `daily_missions` - Daily tasks
- `achievements` - Badges

See `schema.sql` for complete schema.

## 🚀 Running the Application

```bash
# Build project
mvn clean compile

# Run application
mvn javafx:run

# Or from IDE
# Run: com.interviewai.main.MainApp
```

## 🐛 Troubleshooting

### Database Connection Failed
```bash
mysql -u root -p -e "SELECT 1"
# Verify config.properties credentials
```

### FXML Loading Error
```bash
mvn clean compile
rm -rf ~/.m2/repository  # Clear cache if needed
```

### CSS Not Applied
```bash
mvn clean javafx:run
```

## 📚 Project Structure

```
InterviewAI/
├── README.md
├── PROJECT_DOCUMENTATION.md
├── IMPLEMENTATION_GUIDE.md
├── pom.xml
├── src/main/
│   ├── java/com/interviewai/
│   │   ├── controller/
│   │   ├── dao/
│   │   ├── model/
│   │   ├── service/
│   │   ├── util/
│   │   ├── enums/
│   │   └── main/
│   └── resources/
│       ├── fxml/
│       ├── css/
│       ├── config/
│       ├── db/
│       └── images/
└── target/
```

## 🎯 Learning Paths

Follow a proven sequence to master interviews:

### Stage 1: Self-Introduction
- Who are you?
- Professional background
- Key achievements
- Why this company?

### Stage 2: Strengths
- Identify key strengths
- Communicate effectively
- Provide examples
- Back claims with evidence

### Stage 3: Weaknesses
- Turn challenges into lessons
- Growth mindset approach
- Specific examples
- Improvement strategies

### Stage 4: Technical Questions
- System design
- Algorithms & data structures
- Problem-solving approach
- Technical communication

### Stage 5: HR Questions
- Behavioral questions
- Conflict resolution
- Team collaboration
- Leadership examples

### Stage 6: Final Mock Interview
- Full 45-minute simulation
- Mixed question types
- Real-time feedback
- Performance scoring

## 🏆 Gamification Details

### XP System
- Complete lesson: **+5 XP**
- Practice mock interview: **+10 XP**
- Maintain streak: **+15 XP** (daily)
- Review feedback: **+3 XP**

### Daily Missions Examples
- ☐ Practice 1 mock interview (+10 XP)
- ☐ Finish 1 lesson (+5 XP)
- ☐ Keep your 3-day streak (+15 XP)

### Achievement Badges
- 🏆 **Consistency Hero** - 7-day streak
- 💬 **Communication Master** - Master communication lessons
- 🧠 **Technical Expert** - Excel in technical section
- 🎤 **Confidence Builder** - Complete 5 mock interviews
- ⚡ **Speed Demon** - Answer 20 questions in 10 minutes

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/hassan-elkhatoury/InterviewAI/issues)
- **Email**: hassan.elkhatoury@example.com
- **Full Docs**: See PROJECT_DOCUMENTATION.md

## 📄 License

MIT License - See LICENSE file for details

---

**Version**: 0.1.0  
**Last Updated**: November 6, 2025  
**Status**: Active Development

Made with ❤️ to help you ace your interviews! 🚀
