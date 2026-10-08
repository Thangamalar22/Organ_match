import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches

os.makedirs('report_images', exist_ok=True)

plt.rcParams['font.sans-serif'] = 'Times New Roman'
plt.rcParams['font.family'] = 'serif'

# -------------------------------------------------------------
# 1. Architectural Diagram (Black and White)
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 8), dpi=300)
ax.set_xlim(0, 100)
ax.set_ylim(0, 100)
ax.axis('off')

# Title
ax.text(50, 96, "SYSTEM ARCHITECTURE - MULTI-ORGAN CSP MATCHING PLATFORM", 
        ha='center', va='center', fontsize=14, fontweight='bold', color='black')

# Outer box
outer_box = patches.Rectangle((2, 2), 96, 90, linewidth=2, edgecolor='black', facecolor='white')
ax.add_patch(outer_box)

# Layers
layers = [
    ("PRESENTATION LAYER (UI & REST API)", 74, 16, [
        ("Web Controllers (Thymeleaf)", 8, 77, 26, 8),
        ("REST API Controllers", 37, 77, 26, 8),
        ("Security Interceptors", 66, 77, 26, 8)
    ]),
    ("BUSINESS & SECURITY LAYER", 50, 16, [
        ("Matching Service", 8, 53, 26, 8),
        ("Experiment Runner", 37, 53, 26, 8),
        ("Audit Logger & Security", 66, 53, 26, 8)
    ]),
    ("CORE AI & CSP SOLVER ENGINE", 26, 16, [
        ("CSPSolver Engine (MRV/LCV/FC)", 5, 29, 28, 8),
        ("Candidate Elimination Service", 36, 29, 28, 8),
        ("Multi-Criteria Utility Scorer", 67, 29, 28, 8)
    ]),
    ("DATA & PERSISTENCE LAYER", 6, 14, [
        ("Spring Data JPA Repositories", 12, 8, 35, 7),
        ("H2 Database Engine / Data Generator", 53, 8, 35, 7)
    ])
]

for title, y_pos, height, components in layers:
    # Layer container box
    box = patches.Rectangle((4, y_pos), 92, height, linewidth=1.5, edgecolor='black', facecolor='#f5f5f5')
    ax.add_patch(box)
    ax.text(6, y_pos + height - 3, title, fontsize=10, fontweight='bold', color='black')
    
    # Sub-components
    for c_title, cx, cy, cw, ch in components:
        c_box = patches.Rectangle((cx, cy), cw, ch, linewidth=1, edgecolor='black', facecolor='white')
        ax.add_patch(c_box)
        ax.text(cx + cw/2, cy + ch/2, c_title, ha='center', va='center', fontsize=8, fontweight='bold', color='black')

# Vertical Connectors (Arrows)
arrow_style = dict(arrowstyle="<->", color="black", lw=1.5)
ax.annotate("", xy=(50, 74), xytext=(50, 66), arrowprops=arrow_style)
ax.annotate("", xy=(50, 50), xytext=(50, 42), arrowprops=arrow_style)
ax.annotate("", xy=(50, 26), xytext=(50, 20), arrowprops=arrow_style)

plt.tight_layout()
plt.savefig('report_images/diagram_architecture_bw.png', dpi=300)
plt.close()
print('Generated diagram_architecture_bw.png')

# -------------------------------------------------------------
# 2. CSP Multi-Organ Matching Data Flow (Black and White)
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 7), dpi=300)
ax.set_xlim(0, 100)
ax.set_ylim(0, 100)
ax.axis('off')

ax.text(50, 95, "CSP MULTI-ORGAN MATCHING DATA FLOW & PIPELINE", 
        ha='center', va='center', fontsize=14, fontweight='bold', color='black')

steps = [
    ("1. Donor & Recipient Ingestion", "Fetch active donor organ units & waiting list candidates from DB", 10, 80),
    ("2. Node Consistency Filtering", "Unary constraints: Blood Group, Ischemia Time, Crossmatch, Size Ratio", 10, 64),
    ("3. Backtracking Search Engine", "Variable Selection (MRV + Degree) & Value Ordering (LCV)", 10, 48),
    ("4. Forward Checking & Pruning", "Early wipe-out detection & Branch-and-Bound score bound pruning", 10, 32),
    ("5. Optimal Allocation & Explanation", "Global Multi-Organ Allocation Matrix with Decision Audit Explanations", 10, 16)
]

for title, desc, x, y in steps:
    box = patches.Rectangle((x, y), 80, 11, linewidth=1.5, edgecolor='black', facecolor='#f8f8f8')
    ax.add_patch(box)
    ax.text(x + 2, y + 7, title, fontsize=11, fontweight='bold', color='black')
    ax.text(x + 2, y + 3, desc, fontsize=9, color='black')

for y in [79, 63, 47, 31]:
    ax.annotate("", xy=(50, y-4), xytext=(50, y), arrowprops=dict(arrowstyle="->", color="black", lw=1.5))

plt.tight_layout()
plt.savefig('report_images/diagram_dataflow_bw.png', dpi=300)
plt.close()
print('Generated diagram_dataflow_bw.png')

# -------------------------------------------------------------
# 3. Candidate Elimination Flowchart (Black and White)
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(9, 9), dpi=300)
ax.set_xlim(0, 100)
ax.set_ylim(0, 100)
ax.axis('off')

ax.text(50, 96, "CANDIDATE ELIMINATION ALGORITHM FLOWCHART", 
        ha='center', va='center', fontsize=13, fontweight='bold', color='black')

# Flowchart elements: Start, Decision diamonds, Action boxes, End
boxes = [
    ("Start Candidate Evaluation", 35, 88, 30, 6, "ellipse"),
    ("ABO Compatible?", 35, 74, 30, 8, "diamond"),
    ("Medically Fit?", 35, 58, 30, 8, "diamond"),
    ("Size & Transport Valid?", 35, 42, 30, 8, "diamond"),
    ("HLA / Crossmatch Pass?", 35, 26, 30, 8, "diamond"),
    ("Retained in Domain D(Vi)", 32, 10, 36, 7, "rectangle"),
    ("Eliminated (Log Audit)", 75, 50, 22, 12, "rectangle")
]

for title, x, y, w, h, btype in boxes:
    if btype == "ellipse":
        box = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.3", linewidth=1.5, edgecolor='black', facecolor='white')
    elif btype == "diamond":
        # Draw diamond using Polygon
        pts = [[x + w/2, y + h], [x + w, y + h/2], [x + w/2, y], [x, y + h/2]]
        box = patches.Polygon(pts, closed=True, linewidth=1.5, edgecolor='black', facecolor='#f0f0f0')
    else:
        box = patches.Rectangle((x, y), w, h, linewidth=1.5, edgecolor='black', facecolor='white')
    ax.add_patch(box)
    ax.text(x + w/2, y + h/2, title, ha='center', va='center', fontsize=8, fontweight='bold', color='black')

# Arrows
ax.annotate("", xy=(50, 82), xytext=(50, 88), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
ax.annotate("", xy=(50, 66), xytext=(50, 74), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
ax.text(51, 71, "Yes", fontsize=8, fontweight='bold')
ax.annotate("", xy=(50, 50), xytext=(50, 58), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
ax.text(51, 55, "Yes", fontsize=8, fontweight='bold')
ax.annotate("", xy=(50, 34), xytext=(50, 42), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
ax.text(51, 39, "Yes", fontsize=8, fontweight='bold')
ax.annotate("", xy=(50, 17), xytext=(50, 26), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
ax.text(51, 22, "Pass", fontsize=8, fontweight='bold')

# Rejection arrows to "Eliminated"
for y_start in [78, 62, 46, 30]:
    ax.annotate("", xy=(75, 56), xytext=(65, y_start), arrowprops=dict(arrowstyle="->", color="black", lw=1, connectionstyle="arc3,rad=-0.2"))
    ax.text(66, y_start + 1, "No", fontsize=7, fontweight='bold')

plt.tight_layout()
plt.savefig('report_images/diagram_candidate_elimination_bw.png', dpi=300)
plt.close()
print('Generated diagram_candidate_elimination_bw.png')

# -------------------------------------------------------------
# 4. Multi-Criteria Utility Scoring Hierarchy (Black and White)
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 6), dpi=300)
ax.set_xlim(0, 100)
ax.set_ylim(0, 100)
ax.axis('off')

ax.text(50, 95, "MULTI-CRITERIA UTILITY SCORING HIERARCHY", 
        ha='center', va='center', fontsize=13, fontweight='bold', color='black')

# Root box
root = patches.Rectangle((30, 80), 40, 10, linewidth=1.5, edgecolor='black', facecolor='#e0e0e0')
ax.add_patch(root)
ax.text(50, 85, "Total Candidate Utility Score U(Rj)", ha='center', va='center', fontsize=10, fontweight='bold')

leafs = [
    ("Medical Urgency (40%)", "Urgency Status Score", 2, 40),
    ("Waiting Time (20%)", "Normalized Wait Days", 21, 40),
    ("HLA Matching (20%)", "6-Locus Antigen Match Ratio", 41, 40),
    ("Age Benefit (10%)", "Pediatric/Life Years Saved", 61, 40),
    ("Transport Penalty (10%)", "Cold Ischemia Time Loss", 81, 40)
]

for title, desc, x, y in leafs:
    box = patches.Rectangle((x, y), 17, 18, linewidth=1.2, edgecolor='black', facecolor='white')
    ax.add_patch(box)
    ax.text(x + 8.5, y + 13, title, ha='center', va='center', fontsize=8, fontweight='bold')
    ax.text(x + 8.5, y + 6, desc, ha='center', va='center', fontsize=7, style='italic', wrap=True)
    
    # Connecting line from root
    ax.annotate("", xy=(x + 8.5, y + 18), xytext=(50, 80), arrowprops=dict(arrowstyle="-", color="black", lw=1))

plt.tight_layout()
plt.savefig('report_images/diagram_utility_scoring_bw.png', dpi=300)
plt.close()
print('Generated diagram_utility_scoring_bw.png')

print('All 4 black and white diagrams generated successfully!')
