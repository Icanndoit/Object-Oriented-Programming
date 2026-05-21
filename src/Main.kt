package humanmade

fun main() {
    // 1. 게임 초기 설정
    val deck = Deck()
    deck.setupDeck()
    deck.shuffle()

    val tables = Array(6) { i -> Table(i + 1) }

    val players = arrayOf(
        Player(Color.RED),
        Player(Color.BLUE),
        Player(Color.YELLOW)
    )

    println("=== 라스베가스 게임을 시작합니다 ===")

    // 2. 총 4라운드 진행
    for (round in 1..4) {
        println("\n==================================")
        println("           [라운드 $round]           ")
        println("==================================")

        // [핵심 수정] init 블록을 뺐으므로, 매 라운드 시작할 때 여기서 직접 주사위 8개를 채워줍니다.
        for (player in players) {
            player.resetDiceInventory()
        }

        // [라운드 준비] 각 테이블에 지폐 분배
        println("\n--- 카지노 지폐 세팅 ---")
        for (i in 0 until tables.size) {
            tables[i].distributeCash(deck)
        }

        // [턴 진행] 모든 플레이어가 주사위를 소진할 때까지 반복
        while (true) {
            var allDiceUsed = true

            for (player in players) {
                if (player.hasDice) {
                    allDiceUsed = false
                    player.rollAndBet(tables)
                }
            }

            if (allDiceUsed) {
                break
            }
        }

        // [라운드 종료] 각 카지노 테이블별로 상금 정산
        println("\n--- 라운드 $round 정산 ---")
        for (i in 0 until tables.size) {
            tables[i].calculateReward(players, deck)
        }
    }

    // 3. 최종 우승자 판별
    println("\n==================================")
    println("           [최종 결과]           ")
    println("==================================")

    var winner = players[0]

    for (i in 0 until players.size) {
        val money = players[i].getTotalMoney()
        val count = players[i].getCashCount()
        println("${players[i].color} 플레이어: 총 ₩${money} (지폐 ${count}장)")

        if (i == 0) continue

        val currentWinnerMoney = winner.getTotalMoney()
        val playerMoney = players[i].getTotalMoney()

        if (playerMoney > currentWinnerMoney) {
            winner = players[i]
        }
        else if (playerMoney == currentWinnerMoney) {
            if (players[i].getCashCount() > winner.getCashCount()) {
                winner = players[i]
            }
        }
    }

    println("\n최종 우승자는 ${winner.color} 플레이어입니다! 축하합니다!")
}