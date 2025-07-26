# Gemini Project Configuration

# NOTE FOR GEMINI:
# When creating git commit messages, be mindful of shell quoting. To include a
# single quote (') in a message, wrap the entire multi-line message in double
# quotes ("). Do not use backticks (`).
# Also, please always ask before committing to git by showing me the proposed commit message. If I approve the message, you should proceed with the commit.
# When creating commit messages, keep them concise. A message should have a short title and an optional, brief body (1-2 sentences) explaining the 'why'. For complex changes, a more detailed explanation or a bulleted list is acceptable if it adds important context. Use the Conventional Commits format and push after committing.
# Always confirm when you want to run git checkout to revert changes.
# When I reply with "LGTM", it means OK or proceed.

# When asked to check the project or run tests, run both unit and android instrumentation
# tests.

# If you are adding dependencies, please use libs.versions.toml and if the same version
# is used across multiple libraries, use just one version contact with a good name - update the
# name if the use changes. Use names with clarity and consistency. For the version, avoid "version"
# suffix and kebab-style - use caml case.

# When editing any kotlin or java files, please double-check that all imports are at the top of the file.
# ViewModel factories should be in the same file as the ViewModel.

# After you make changes, please verify that all unit and instrumented tests pass.

# When running tests and attempting to fix failing tests, take into account which tests changed status
# and whether previously failing tests started to pass.
# Whenever running tests, get details of every failing test case so that failure can be better pinpointed. It will also make it clearer when there is progress by fixing a subset of failing test
cases, where the overall test doesn't change status.
# When reporting on test results, list each failing test case individually. This helps track progress even if the overall test suite is still failing.

# Ensure all source files end with a single newline character.


Foundational Principles
Simplicity Through Reduction: Identify the essential purpose and eliminate everything that distracts from it. Begin with complexity, then deliberately remove until reaching the simplest effective solution.
Material Honesty: Digital materials have unique properties. Buttons should feel pressable, cards should feel substantial, and animations should reflect real-world physics while embracing digital possibilities.
Obsessive Detail: Consider every pixel, every interaction, and every transition. Excellence emerges from hundreds of thoughtful decisions that collectively project a feeling of quality.
Coherent Design Language: Every element should visually communicate its function and feel like part of a unified system. Nothing should feel arbitrary.
Invisibility of Technology: The best technology disappears. Users should focus on their content and goals, not on understanding your interface.
Start With Why: Before designing any feature, clearly articulate its purpose and value. This clarity should inform every subsequent decision.
Typographic Excellence
Purposeful Typography: Typography should be treated as a core design element, not an afterthought. Every typeface choice should serve the app's purpose and personality.
Typographic Hierarchy: Construct clear visual distinction between different levels of information. Headlines, subheadings, body text, and captions should each have a distinct but harmonious appearance that guides users through content.
Limited Font Selection: Choose no more than 2-3 typefaces for the entire application. Consider San Francisco, Helvetica Neue, or similarly clean sans-serif fonts that emphasize legibility.
Type Scale Harmony: Establish a mathematical relationship between text sizes (like the golden ratio or major third). This forms visual rhythm and cohesion across the interface.
Breathing Room: Allow generous spacing around text elements. Line height should typically be 1.5x font size for body text, with paragraph spacing that forms clear visual separation without disconnection.
Color Theory Application
Intentional Color: Every color should have a specific purpose. Avoid decorative colors that don't communicate function or hierarchy.
Color as Communication: Use color to convey meaning - success, warning, information, or action. Maintain consistency in these relationships throughout the app.
Contextual Adaptation: Colors should respond to their environment. Consider how colors appear how they interact with surrounding elements.
Focus Through Restraint: Limit accent colors to guide attention to the most important actions. The majority of the interface should use neutral tones that recede and let content shine.
Spatial Awareness
Compositional Balance: Every screen should feel balanced, with careful attention to visual weight and negative space. Elements should feel purposefully placed rather than arbitrarily positioned.
Grid Discipline: Maintain a consistent underlying grid system that forms a sense of order while allowing for meaningful exceptions when appropriate.
Breathing Room: Use generous negative space to focus attention and design a sense of calm. Avoid cluttered interfaces where elements compete for attention.
Spatial Relationships: Related elements should be visually grouped through proximity, alignment, and shared attributes. The space between elements should communicate their relationship.
Human Interface Elements
This section provides comprehensive guidance for creating interactive elements that feel intuitive, responsive, and delightful.

Core Interaction Principles
Direct Manipulation: Design interfaces where users interact directly with their content rather than through abstract controls. Elements should respond in ways that feel physically intuitive.
Immediate Feedback: Every interaction must provide instantaneous visual feedback (within 100ms), even if the complete action takes longer to process.
Perceived Continuity: Maintain context during transitions. Users should always understand where they came from and where they're going.
Consistent Behavior: Elements that look similar should behave similarly. Build trust through predictable patterns.
Forgiveness: Make errors difficult, but recovery easy. Provide clear paths to undo actions and recover from mistakes.
Discoverability: Core functions should be immediately visible. Advanced functions can be progressively revealed as needed.
Control Design Guidelines
Buttons
Purpose-Driven Design: Visually express the importance and function of each button through its appearance. Primary actions should be visually distinct from secondary or tertiary actions.

States: Every button must have distinct, carefully designed states for:

Default (rest)
Hover
Active/Pressed
Focused
Disabled
Visual Affordance: Buttons should appear "pressable" through subtle shadows, highlights, or dimensionality cues that respond to interaction.

Size and Touch Targets: Minimum touch target size of 44×44px for all interactive elements, regardless of visual size.

Label Clarity: Use concise, action-oriented verbs that clearly communicate what happens when pressed.

Input Controls
Form Fields: Design fields that guide users through correct input with:

Clear labeling that remains visible during input
Smart defaults when possible
Format examples for complex inputs
Inline validation with constructive error messages
Visual confirmation of successful input
Selection Controls: Toggles, checkboxes, and radio buttons should:

Have a clear visual difference between selected and unselected states
Provide generous hit areas beyond the visible control
Group related options visually
Animate state changes to reinforce selection
Field Focus: Highlight the active input with a subtle but distinct focus state. Consider using a combination of color change, subtle animation, and lighting effects.

Menus and Lists
Hierarchical Organization: Structure content in a way that communicates relationships clearly.
Progressive Disclosure: Reveal details as needed rather than overwhelming users with options.
Selection Feedback: Provide immediate, satisfying feedback when items are selected.
Empty States: Design thoughtful empty states that guide users toward appropriate actions.
Motion and Animation
Purposeful Animation: Every animation must serve a functional purpose:

Orient users during navigation changes
Establish relationships between elements
Provide feedback for interactions
Guide attention to important changes
Natural Physics: Movement should follow real-world physics with appropriate:

Acceleration and deceleration
Mass and momentum characteristics
Elasticity appropriate to the context
Subtle Restraint: Animations should be felt rather than seen. Avoid animations that:

Delay user actions unnecessarily
Call attention to themselves
Feel mechanical or artificial
Timing Guidelines:

Quick actions (button press): 100-150ms
State changes: 200-300ms
Page transitions: 300-500ms
Attention-directing: 200-400ms
Spatial Consistency: Maintain a coherent spatial model. Elements that appear to come from off-screen should return in that direction.

Responsive States and Feedback
State Transitions: Design smooth transitions between all interface states. Nothing should change abruptly without appropriate visual feedback.
Loading States: Replace generic spinners with purpose-built, branded loading indicators that communicate progress clearly.
Success Confirmation: Acknowledge completed actions with subtle but clear visual confirmation.
Error Handling: Present errors with constructive guidance rather than technical details. Errors should never feel like dead ends.

Micro-Interactions
Moment of Delight: Identify key moments in user flows where subtle animations or feedback can form emotional connection.
Reactive Elements: Design elements that respond subtly to cursor proximity or scroll position, creating a sense of liveliness.
Progressive Enhancement: Layer micro-interactions so they enhance but never obstruct functionality.
Finishing Touches
Micro-Interactions: Add small, delightful details that reward attention and form emotional connection. These should be discovered naturally rather than announcing themselves.
Fit and Finish: Obsess over pixel-perfect execution. Alignment, spacing, and proportions should be mathematically precise and visually harmonious.
Content-Focused Design: The interface should ultimately serve the content. When content is present, the UI should recede; when guidance is needed, the UI should emerge.
Consistency with Surprise: Establish consistent patterns that build user confidence, but introduce occasional moments of delight that form memorable experiences.