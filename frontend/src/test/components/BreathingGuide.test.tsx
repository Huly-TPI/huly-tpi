import { render, screen, fireEvent, act } from '@testing-library/react';
import {BreathingGuide} from '../../components/BreathingGuide';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';


describe('BreathingGuide', () => {
      it('muestra botón para iniciar ejercicios tras seleccionar técnica', async () => {
        const user = userEvent.setup()
        render(<BreathingGuide />)
        await user.click(screen.getByRole('button', { name: /diafragmática/i }))
        expect(screen.getByRole('button', { name: /iniciar/i })).toBeInTheDocument()
      })
        
      it('muestra las técnicas de respiración disponibles', () => {
        render(<BreathingGuide />)
        expect(screen.getByText(/diafragmática/i)).toBeInTheDocument()
        expect(screen.getByText(/cuadrada/i)).toBeInTheDocument()
      })
      it('muestra la descripción de cada técnica de respiración', () => {
        render(<BreathingGuide />)
        expect(screen.getByText(/Inhala profundamente por la nariz/i)).toBeInTheDocument()
        expect(screen.getByText(/Inhala, mantén la respiración/i)).toBeInTheDocument()

      })
      it('al seleccionar una tecnica oculta la selección y muestra el ejercicio', async () => {
          const user = userEvent.setup()
          render(<BreathingGuide />)
          await user.click(screen.getByRole('button', { name: /diafragmática/i }))
          expect(screen.queryByText(/selecciona una técnica/i)).not.toBeInTheDocument()
          })
      it('al iniciar el ejercicio muestra la primera fase', async () => {
        const user = userEvent.setup()
        render(<BreathingGuide />)
        await user.click(screen.getByRole('button', { name: /diafragmática/i }))
        await user.click(screen.getByRole('button', { name: /iniciar/i }))
        expect(screen.queryByText(/inhala profundamente/i)).not.toBeInTheDocument()
        expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
      })
        it('al iniciar el ejercicio muestra la primera fase', async () => {
        const user = userEvent.setup()
        render(<BreathingGuide />)
        await user.click(screen.getByRole('button', { name: /diafragmática/i }))
        await user.click(screen.getByRole('button', { name: /iniciar/i }))
        expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
      })
      it('muestra el tiempo restante de la fase actual al iniciar el ejercicio', async () => { 
        render(<BreathingGuide />  )
        fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
        fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
        expect(screen.getByText(/4/i)).toBeInTheDocument()
      })
      it('el temporizador descuenta segundo por segundo', () => {
      vi.useFakeTimers()
        render(<BreathingGuide />  )
        fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
        fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
        expect(screen.getByText(/4/i)).toBeInTheDocument()
        act(() => {    vi.advanceTimersByTime(1000)
        } )
        expect(screen.getByText(/3/i)).toBeInTheDocument()
      vi.useRealTimers()
        })

        it('al finalizar una fase pasa a la siguiente', () => {
          vi.useFakeTimers()
            render(<BreathingGuide />  )
            fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
            fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
            expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
            act(() => {    vi.advanceTimersByTime(1000)})
            act(() => {    vi.advanceTimersByTime(1000)})
            act(() => {    vi.advanceTimersByTime(1000)})
            act(() => {    vi.advanceTimersByTime(1000)})
            expect(screen.getByText(/sostenélo/i)).toBeInTheDocument()
          vi.useRealTimers()
            })
            
        it('al terminar el ciclo se inicia el ciclo completo') , () => {
          vi.useFakeTimers()
              render(<BreathingGuide/>)
              fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
              fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
              expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
              for(let i = 0; i < 12; i++) {
                act(() => {    vi.advanceTimersByTime(1000)}  
                  )   }
              expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
            vi.useRealTimers()
      
          }

          it('al hacer click en volver regresa a la selección de tecnicas', async () => {
            const user = userEvent.setup()
            render(<BreathingGuide />)
            await user.click(screen.getByRole('button', { name: /diafragmática/i }))
            await user.click(screen.getByRole('button', { name: /volver/i }))
            expect(screen.getByText(/selecciona una técnica/i)).toBeInTheDocument()
          })

         it('al terminar todas las rondas regresa a la selecciónd e técnicas', () => {
          const shortTechnique = {
            id: 3,
            name: 'Corta',
            description: 'Inhala por 1 segundo, sostene por 1 segundo, exhala por 1 segundo.',
            inhale_seconds: 1,
            hold_seconds: 1,
            exhale_seconds: 1,
            rounds_interval: 1,
            round: 2,
          }
          vi.useFakeTimers()
              render(<BreathingGuide techniques={[shortTechnique]} />)
              fireEvent.click(screen.getByRole('button', { name: /corta/i }))
              fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
              expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
                
              for(let i = 0; i < 6; i++) {
                act(() => {    vi.advanceTimersByTime(1000)}  
                  )   } 
              expect(screen.getByText(/selecciona una técnica/i)).toBeInTheDocument()
              vi.useRealTimers()
            })

        it('muestra botón volver durante el ejercicio', async () => {
          render(<BreathingGuide />) 
          fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
          fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
          expect(screen.getByRole('button', { name: /volver/i })).toBeInTheDocument()
        })

        it('aplica la clase CSS correcta según la fase activa', async () => {
          render(<BreathingGuide />) 
          fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
          fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
          const circle = screen.getByTestId('breathing-circle')
          expect(circle).toHaveClass('inhale') 
          })
        });

