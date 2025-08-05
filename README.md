# 📊 Calculando O Silêncio - Gestão Financeira Pessoal

Um aplicativo Android desenvolvido em Kotlin para controle financeiro inteligente, com análise de gastos, geração de relatórios em PDF e visualização gráfica.

(Porque até o silêncio tem seu preço! 💰)
---------------------------------------------------------------------------------------
✨ Funcionalidades Principais:

💰 Gestão de Transações

    Cadastro de entradas/saídas com descrição, valor e categoria

    Edição e exclusão de transações

    Filtro por mês/ano

    Cálculo automático do saldo mensal
---------------------------------------------------------------------------------------
📈 Visualização Gráfica

    Gráficos mensais (MPAndroidChart)

    Média anual de gastos

    Distribuição por categorias
---------------------------------------------------------------------------------------
📄 Relatórios em PDF

    Geração de PDFs mensais/anuais com iTextPDF

    Layout profissional com tabelas e totais

    Salvamento automático em /Downloads/
---------------------------------------------------------------------------------------
💾 Armazenamento Local (Room Database)
kotlin

@Entity(tableName = "transactions")

data class Transaction(

    @PrimaryKey(autoGenerate = true) val id: Int,
    
    val description: String,
    
    val amount: Double,  // Negativo para saídas
    
    val category: String,
    
    val month: Int,
    
    val year: Int
    
)
---------------------------------------------------------------------------------------
🛠️ Tecnologias Utilizadas

Componente    	Detalhes

Linguagem	      Kotlin 1.9

UI	            Jetpack Compose (100% declarativo)

Arquitetura	    MVVM + Clean Architecture
Banco de Dados	Room Database + Coroutines

PDF	            iTextPDF 7.2.3

Gráficos	      MPAndroidChart v3.1.0

---------------------------------------------------------------------------------------
📂 Estrutura do Projeto

CalculandoOSilencio/

├── app/

│   ├── src/main/

│   │   ├── java/com/example/calculandoosilencio/

│   │   │   ├── data/              # Room (Dao, Database, Entities, PdfExporter)

│   │   │   ├── ui/                # Screens (TransactionScreen, TransactionViewModel)

│   │   │   ├── components/        # Gráficos, Selectors (MonthYearSelector)

│   │   │   ├── theme/             # Color, Theme, Type

│   │   │   └── viewmodel/         # TransactionViewModel

│   │   └── res/                   # Drawables, Strings...

├── build.gradle.kts               # Configuração principal

---------------------------------------------------------------------------------------
🔧 Dependências Críticas
kotlin

// Room + Coroutines
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// iTextPDF
implementation("com.itextpdf:itext7-core:7.2.3")
implementation("com.itextpdf:layout:7.2.3")

// Gráficos
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.02.01"))
implementation("androidx.compose.material3:material3")
---------------------------------------------------------------------------------------
🎨 UI/UX

    Tema escuro com cores personalizadas (AccentPZ, BlackPZ)

    Formulário dinâmico com validação

    Botões de ação:

        ➕ ADICIONAR / ✏️ ATUALIZAR

        📊 Gráficos / 📄 PDF

https://i.imgur.com/example.png (Layout ilustrativo)
---------------------------------------------------------------------------------------
⚡ Como Executar

    Clone o repositório

    Adicione em local.properties (opcional):
    properties

PDF_EXPORT_PATH=/storage/emulated/0/Download/

Build:
bash

    ./gradlew assembleDebug
---------------------------------------------------------------------------------------
🔮 Roadmap Futuro

Feature                    	Status

Sincronização com nuvem	    🚀 Em breve

Orçamento mensal	         💡 Planejado
---------------------------------------------------------------------------------------
📜 Licença

Projeto open-source sob MIT License.

Desenvolvido com ❤️ (e muito café) em Rondonópolis/MT!
