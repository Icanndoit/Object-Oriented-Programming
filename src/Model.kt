package humanmade

// 색상 상수 정의
enum class Color {
    RED, BLUE, YELLOW, GREEN, ORANGE
}

// 테이블 배치 동작을 위한 인터페이스
interface Placeable {
    fun place(casinoNumber: Int)
}

// 지폐 객체 (데이터 중심)
data class Cash(val value: Int) : Placeable {
    override fun place(casinoNumber: Int) {
        println("₩${value} 지폐가 ${casinoNumber}번 카지노에 배치되었습니다.")
    }
}

// 주사위 객체 (상태 변경)
class Dice(val color: Color) : Placeable {
    // 외부 수정 방지
    var currentNumber: Int = 1
        private set

    // 1~6 랜덤 눈금 생성 및 저장
    fun roll(): Int {
        currentNumber = (1..6).random()
        return currentNumber
    }

    override fun place(casinoNumber: Int) {
        println("${color}색 주사위가 ${casinoNumber}번 카지노에 배치되었습니다.")
    }
}

// 플레이어 객체
class Player(val color: Color) {
    val diceInventory = mutableListOf<Dice>()
    private val account = mutableListOf<Cash>()

    // 주사위 보유 여부 반환
    val hasDice: Boolean
        get() = diceInventory.isNotEmpty()

    // 턴 진행 (주사위 굴리기 및 배팅)
    fun rollAndBet(tables: Array<Table>) {
        println("--- ${color} 플레이어의 차례입니다. 주사위를 굴리려면 Enter를 누르세요. ---")
        readLine()

        // 남은 주사위를 모두 굴려 결과 배열에 저장
        val diceResult = IntArray(diceInventory.size)
        print("나온 눈금: ")
        for (i in 0 until diceInventory.size) {
            diceResult[i] = diceInventory[i].roll()
            print("${diceResult[i]} ")
        }
        println()

        var betTmp = -1
        // 배팅할 카지노 번호 입력 및 검증
        while (true) {
            print("배팅할 카지노 번호를 입력하세요: ")
            betTmp = readLine()?.toIntOrNull() ?: -1

            var hasNumber = false
            for (i in 0 until diceResult.size) {
                if (diceResult[i] == betTmp) {
                    hasNumber = true
                    break
                }
            }

            if (betTmp in 1..6 && hasNumber) {
                break
            } else {
                println("선택한 번호의 주사위가 없거나 잘못된 번호입니다. 다시 입력해주세요.")
            }
        }

        // 선택한 눈금과 일치하는 주사위 개수 카운트
        var diceTmp = 0
        for (i in 0 until diceResult.size) {
            if (diceResult[i] == betTmp) {
                diceTmp++
            }
        }

        // 선택한 주사위는 테이블로 이동, 나머지는 인벤토리에 유지
        val diceToKeep = mutableListOf<Dice>()
        for (dice in diceInventory) {
            if (dice.currentNumber == betTmp) {
                tables[betTmp - 1].diceList.add(dice)
            } else {
                diceToKeep.add(dice)
            }
        }

        // 인벤토리 갱신
        diceInventory.clear()
        for (dice in diceToKeep) {
            diceInventory.add(dice)
        }

        println("${color} 플레이어가 ${betTmp}번 카지노에 주사위 ${diceTmp}개를 배치했습니다.")
    }

    // 라운드 시작 시 주사위 8개 초기화
    fun resetDiceInventory() {
        diceInventory.clear()
        for (i in 1..8) {
            diceInventory.add(Dice(color))
        }
    }

    // 획득한 지폐 추가
    fun receiveCash(cash: Cash) {
        account.add(cash)
    }

    // 보유 금액 총합 계산
    fun getTotalMoney(): Int {
        var sum = 0
        for (cash in account) {
            sum += cash.value
        }
        return sum
    }

    fun getCashCount(): Int {
        return account.size
    }
}

// 지폐 덱 객체
class Deck {
    private val pileOfCash = mutableListOf<Cash>()

    // 57장 지폐 덱 구성
    fun setupDeck() {
        val values = intArrayOf(10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000)
        val counts = intArrayOf(9, 9, 9, 9, 6, 6, 3, 3, 3)

        for (i in values.indices) {
            for (j in 1..counts[i]) {
                pileOfCash.add(Cash(values[i]))
            }
        }
    }

    // 덱 섞기
    fun shuffle() {
        pileOfCash.shuffle()
    }

    // 덱 맨 아래에서 한 장 뽑기
    fun draw(): Cash {
        return pileOfCash.removeLast()
    }

    // 지폐를 덱 맨 아래로 반환
    fun returnToBottom(cash: Cash) {
        pileOfCash.add(0, cash)
    }
}

// 카지노 테이블 객체
class Table(val casinoNumber: Int) {
    val diceList = mutableListOf<Dice>()
    val cashList = mutableListOf<Cash>()

    // 합계가 50000 이상이 될 때까지 지폐 배치 및 내림차순 정렬
    fun distributeCash(deck: Deck) {
        var sum = 0
        while (sum < 50000) {
            val cashTmp = deck.draw()
            println("₩${cashTmp.value} 지폐가 ${casinoNumber}번 카지노에 배치되었습니다.")
            cashList.add(cashTmp)
            sum += cashTmp.value
        }

        // 지폐 내림차순 버블 정렬
        for (i in 0 until cashList.size) {
            for (j in 0 until cashList.size - 1 - i) {
                if (cashList[j].value < cashList[j + 1].value) {
                    val temp = cashList[j]
                    cashList[j] = cashList[j + 1]
                    cashList[j + 1] = temp
                }
            }
        }
    }

    // 라운드 종료 후 상금 정산
    fun calculateReward(players: Array<Player>, deck: Deck) {
        // 주사위가 없으면 정산 취소
        if (diceList.size == 0) return

        // 배치된 주사위 색상 리스트 생성 및 정렬
        val colorList = mutableListOf<Color>()
        for (dice in diceList) {
            colorList.add(dice.color)
        }
        colorList.sort()

        // 1열: 색상, 2열: 개수를 저장하는 2차원 리스트 생성
        val countTmpList = mutableListOf<Array<Any>>()

        var colorTmp = colorList[0]
        var countTmp = 1

        // 연속된 색상의 개수 카운트
        for (i in 1 until colorList.size) {
            if (colorList[i] == colorTmp) {
                countTmp++
            } else {
                countTmpList.add(arrayOf(colorTmp, countTmp))
                colorTmp = colorList[i]
                countTmp = 1
            }
        }
        countTmpList.add(arrayOf(colorTmp, countTmp))

        // 개수가 중복되는 동점자 색상의 개수를 0으로 무효 처리
        for (i in 0 until countTmpList.size) {
            val count1 = countTmpList[i][1] as Int

            if (count1 == 0) continue

            var isDuplicate = false
            for (j in i + 1 until countTmpList.size) {
                val count2 = countTmpList[j][1] as Int

                if (count1 == count2) {
                    countTmpList[j][1] = 0
                    isDuplicate = true
                }
            }

            if (isDuplicate) {
                countTmpList[i][1] = 0
            }
        }

        // 주사위 개수를 기준으로 내림차순 정렬
        for (i in 0 until countTmpList.size) {
            for (j in 0 until countTmpList.size - 1 - i) {
                val count1 = countTmpList[j][1] as Int
                val count2 = countTmpList[j + 1][1] as Int
                if (count1 < count2) {
                    val temp = countTmpList[j]
                    countTmpList[j] = countTmpList[j + 1]
                    countTmpList[j + 1] = temp
                }
            }
        }

        // 유효한 순위권 색상만 필터링
        val finalList = mutableListOf<Color>()
        for (i in 0 until countTmpList.size) {
            val count = countTmpList[i][1] as Int
            if (count > 0) {
                val color = countTmpList[i][0] as Color
                finalList.add(color)
            }
        }

        // 지폐 개수와 순위권 개수 중 작은 값만큼 분배 진행
        var matchCount = finalList.size
        if (cashList.size < matchCount) {
            matchCount = cashList.size
        }

        // 순위에 맞춰 플레이어에게 지폐 지급
        for (i in 0 until matchCount) {
            val winnerColor = finalList[i]
            val rewardCash = cashList[i]

            for (player in players) {
                if (player.color == winnerColor) {
                    player.receiveCash(rewardCash)
                    println("${casinoNumber}번 카지노: ${winnerColor} 플레이어가 ₩${rewardCash.value} 획득!")
                    break
                }
            }
        }

        // 남은 지폐는 덱으로 반환
        for (i in matchCount until cashList.size) {
            deck.returnToBottom(cashList[i])
        }

        // 테이블 초기화
        cashList.clear()
        diceList.clear()
    }
}